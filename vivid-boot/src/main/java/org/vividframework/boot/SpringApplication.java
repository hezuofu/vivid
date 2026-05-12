package org.vividframework.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.beans.RootBeanDefinition;
import org.vividframework.beans.annotation.Component;
import org.vividframework.web.DispatcherHandler;
import org.vividframework.web.RequestMappingHandlerMapping;
import org.vividframework.web.annotation.RestController;
import org.vividframework.http.server.NettyHttpServer;
import org.vividframework.http.server.AbstractHttpServer;
import org.vividframework.resolver.ViewResolver;
import org.vividframework.view.JsonView;
import org.vividframework.view.RedirectView;
import org.vividframework.event.ApplicationEventPublisher;
import org.vividframework.config.Environment;
import org.vividframework.config.PropertySource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Spring Application style runner
 * @author Jon Fisher
 */
public class SpringApplication {

    private static final Logger logger = LoggerFactory.getLogger(SpringApplication.class);

    private final Class<?> primarySource;
    private Properties defaultProperties = new Properties();
    private Set<String> sources = new LinkedHashSet<>();
    private Set<String> configLocations = new LinkedHashSet<>();
    private boolean webEnvironment = true;
    private GenericApplicationContext applicationContext;
    private AbstractHttpServer webServer;

    public SpringApplication(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    public GenericApplicationContext run(String... args) {
        logger.info("Starting Vivid Application...");

        try {
            // Create application context
            applicationContext = createApplicationContext();

            // Configure environment
            configureEnvironment();

            // Prepare context
            prepareContext(applicationContext);

            // Refresh context
            applicationContext.refresh();

            // Start web server
            if (webEnvironment) {
                startWebServer();
            }

            logger.info("Vivid Application started successfully!");
            return applicationContext;

        } catch (Exception e) {
            logger.error("Failed to start Vivid Application", e);
            throw new RuntimeException("Application startup failed", e);
        }
    }

    protected GenericApplicationContext createApplicationContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setDisplayName("Vivid Application");
        return context;
    }

    protected void configureEnvironment() {
        Environment env = applicationContext.getEnvironment();
        if (env instanceof Environment.StandardEnvironment) {
            ((Environment.StandardEnvironment) env).setProperty("server.port", "8080");
            defaultProperties.forEach((key, value) ->
                    ((Environment.StandardEnvironment) env).setProperty(key.toString(), value.toString()));
        }
    }

    protected void prepareContext(GenericApplicationContext context) {
        // Register web handler
        registerDispatcherHandler(context);

        // Scan and register beans
        scanAndRegisterBeans(context);

        // Register web components
        registerWebComponents(context);

        // Add event publisher
        ApplicationEventPublisher.SimpleApplicationEventPublisher eventPublisher = 
            new ApplicationEventPublisher.SimpleApplicationEventPublisher();
        context.registerBeanDefinition("eventPublisher",
                createBeanDefinition(ApplicationEventPublisher.SimpleApplicationEventPublisher.class, eventPublisher));
    }

    protected void registerDispatcherHandler(GenericApplicationContext context) {
        DispatcherHandler dispatcherHandler = new DispatcherHandler(context);
        context.registerBeanDefinition("dispatcherHandler",
                createBeanDefinition(DispatcherHandler.class, dispatcherHandler));
    }

    protected void scanAndRegisterBeans(GenericApplicationContext context) {
        String basePackage = getBasePackage(primarySource);
        Set<Class<?>> componentClasses = scanComponents(basePackage);

        for (Class<?> clazz : componentClasses) {
            try {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                String beanName = getBeanName(clazz);
                context.registerBeanDefinition(beanName,
                        createBeanDefinition(clazz, instance));
                logger.debug("Registered component: {} as {}", clazz.getName(), beanName);
            } catch (Exception e) {
                logger.warn("Failed to register component: " + clazz.getName(), e);
            }
        }
    }

    protected void registerWebComponents(GenericApplicationContext context) {
        // Register handler mapping
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping(context);
        context.registerBeanDefinition("requestMappingHandlerMapping",
                createBeanDefinition(RequestMappingHandlerMapping.class, handlerMapping));

        // Register view resolvers
        ViewResolver jsonViewResolver = viewName -> {
            if (viewName.startsWith("json:")) {
                return new JsonView();
            }
            return null;
        };
        context.registerBeanDefinition("jsonViewResolver",
                createBeanDefinition(ViewResolver.class, jsonViewResolver));

        ViewResolver redirectViewResolver = viewName -> {
            if (viewName.startsWith("redirect:")) {
                return new RedirectView(viewName.substring(9));
            }
            return null;
        };
        context.registerBeanDefinition("redirectViewResolver",
                createBeanDefinition(ViewResolver.class, redirectViewResolver));
    }

    protected Set<Class<?>> scanComponents(String basePackage) {
        Set<Class<?>> components = new HashSet<>();
        String packagePath = basePackage.replace('.', '/');

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    scanPackageFromFile(resource, basePackage, components);
                } else if ("jar".equals(protocol)) {
                    scanPackageFromJar(resource, basePackage, components);
                }
            }
        } catch (IOException e) {
            logger.warn("Error scanning package: " + basePackage, e);
        }

        return components;
    }

    private void scanPackageFromFile(URL resource, String basePackage, Set<Class<?>> components) {
        // Simplified - in production use ASM or Java compiler
    }

    private void scanPackageFromJar(URL resource, String basePackage, Set<Class<?>> components) {
        // Simplified - in production use JarInputStream
    }

    protected String getBasePackage(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null && !component.value().isEmpty()) {
            return component.value();
        }
        return clazz.getPackage().getName();
    }

    protected String getBeanName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    protected RootBeanDefinition createBeanDefinition(Class<?> type, Object instance) {
        RootBeanDefinition definition = new RootBeanDefinition(type);
        definition.setInstance(instance);
        return definition;
    }

    protected void startWebServer() throws Exception {
        DispatcherHandler dispatcher = applicationContext.getBean("dispatcherHandler", DispatcherHandler.class);

        int port = 8080;
        Environment env = applicationContext.getEnvironment();
        if (env != null) {
            String portStr = env.getProperty("server.port");
            if (portStr != null) {
                port = Integer.parseInt(portStr);
            }
        }

        webServer = NettyHttpServer.builder()
                .port(port)
                .handler(request -> dispatcher.handle(request))
                .build();

        webServer.addShutdownHook();
        webServer.start();
        logger.info("Web server started on port {}", port);
    }

    public void setDefaultProperties(Properties properties) {
        this.defaultProperties = properties;
    }

    public void setWebEnvironment(boolean webEnvironment) {
        this.webEnvironment = webEnvironment;
    }

    public GenericApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public AbstractHttpServer getWebServer() {
        return webServer;
    }
}

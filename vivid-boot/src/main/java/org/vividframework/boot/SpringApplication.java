package org.vividframework.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanDefinitionRegistry;
import org.vividframework.beans.RootBeanDefinition;
import org.vividframework.beans.annotation.Component;
import org.vividframework.beans.annotation.ComponentScan;
import org.vividframework.beans.scanner.ClassPathBeanDefinitionScanner;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.web.DispatcherHandler;
import org.vividframework.web.RequestMappingHandlerMapping;
import org.vividframework.web.event.WebEventPublisher;
import org.vividframework.web.filter.Filter;
import org.vividframework.web.handler.ExceptionHandler;
import org.vividframework.web.handler.ExceptionHandlerRegistry;
import org.vividframework.web.interceptor.HandlerInterceptor;
import org.vividframework.webmvc.ExceptionHandlerExceptionResolver;
import org.vividframework.webmvc.RequestMappingHandlerAdapter;
import org.vividframework.webmvc.ResponseBodyAdvice;
import org.vividframework.server.netty.NettyHttpServer;
import org.vividframework.server.AbstractHttpServer;
import org.vividframework.web.resolver.TemplateViewResolver;
import org.vividframework.web.resolver.ViewResolver;
import org.vividframework.web.view.HtmlView;
import org.vividframework.web.view.JsonView;
import org.vividframework.web.view.RedirectView;
import org.vividframework.web.view.TextView;
import org.vividframework.web.view.XmlView;
import org.vividframework.event.ApplicationEventPublisher;
import org.vividframework.config.Environment;

import java.util.*;

/**
 * Spring Application style runner with auto-configuration support.
 * @author sketch
 */
public class SpringApplication {

    private static final Logger logger = LoggerFactory.getLogger(SpringApplication.class);

    private final Class<?> primarySource;
    private Properties defaultProperties = new Properties();
    private boolean webEnvironment = true;
    private GenericApplicationContext applicationContext;
    private AbstractHttpServer webServer;
    private String[] args;

    // Programmatic registrations
    private int serverPort = 8080;
    private String templatePath = "templates/";
    private boolean scanAnnotations = true;
    private String[] scanPackages;
    private final List<ExceptionHandler<?>> programmaticExceptionHandlers = new ArrayList<>();
    private final List<HandlerInterceptor> programmaticInterceptors = new ArrayList<>();
    private final List<Filter> programmaticFilters = new ArrayList<>();
    private final List<ViewResolver> programmaticViewResolvers = new ArrayList<>();
    private final List<ResponseBodyAdvice> programmaticResponseAdvices = new ArrayList<>();

    public SpringApplication(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    public static void run(Class<?> primarySource, String... args) {
        SpringApplication app = new SpringApplication(primarySource);
        app.run(args);
    }

    public GenericApplicationContext run(String... args) {
        this.args = args;
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
        // Apply default properties
        defaultProperties.forEach((key, value) -> {
            if (env.getProperty(key.toString()) == null) {
                env.setProperty(key.toString(), value.toString());
            }
        });
        
        // Set defaults
        if (env.getProperty("server.port") == null) {
            env.setProperty("server.port", String.valueOf(serverPort));
        }
    }

    protected void prepareContext(GenericApplicationContext context) {
        // Register web components BEFORE dispatcher (so it can find them)
        registerWebComponents(context);

        // Register web handler (depends on web components being registered)
        registerDispatcherHandler(context);

        // Load auto-configurations
        loadAutoConfigurations(context);

        // Scan and register beans
        scanAndRegisterBeans(context);

        // Register programmatic components
        registerProgrammaticComponents(context);

        // Add event publisher
        ApplicationEventPublisher.SimpleApplicationEventPublisher eventPublisher =
            new ApplicationEventPublisher.SimpleApplicationEventPublisher();
        context.registerBeanDefinition("eventPublisher",
                createBeanDefinition(ApplicationEventPublisher.SimpleApplicationEventPublisher.class, eventPublisher));
    }

    protected void loadAutoConfigurations(GenericApplicationContext context) {
        // Load auto-configuration classes
        List<String> autoConfigClasses = loadFactoryNames();
        
        for (String className : autoConfigClasses) {
            try {
                Class<?> configClass = Class.forName(className);
                if (configClass.isAnnotationPresent(Component.class) || 
                    configClass.getName().endsWith("Configuration")) {
                    Object instance = configClass.getDeclaredConstructor().newInstance();
                    String beanName = ClassPathBeanDefinitionScanner.getBeanName(configClass);
                    context.registerBeanDefinition(beanName, new RootBeanDefinition(configClass));
                    logger.debug("Loaded auto-configuration: {}", className);
                }
            } catch (Exception e) {
                logger.debug("Could not load auto-configuration class: {}", className);
            }
        }
    }

    protected List<String> loadFactoryNames() {
        List<String> names = new ArrayList<>();
        try {
            Enumeration<java.net.URL> urls = Thread.currentThread().getContextClassLoader()
                    .getResources("META-INF/vivid.factories");
            while (urls.hasMoreElements()) {
                java.net.URL url = urls.nextElement();
                java.io.InputStream is = url.openStream();
                java.util.Properties props = new java.util.Properties();
                props.load(is);
                is.close();
                
                String value = props.getProperty("org.vividframework.boot.autoconfigure.EnableAutoConfiguration");
                if (value != null) {
                    for (String name : value.split(",")) {
                        names.add(name.trim());
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("No auto-configurations found");
        }
        return names;
    }

    protected void registerDispatcherHandler(GenericApplicationContext context) {
        DispatcherHandler dispatcherHandler = new DispatcherHandler(context);
        context.registerBeanDefinition("dispatcherHandler",
                createBeanDefinition(DispatcherHandler.class, dispatcherHandler));
    }

    protected void scanAndRegisterBeans(GenericApplicationContext context) {
        if (!scanAnnotations) return;

        String[] basePackages;
        if (scanPackages != null) {
            basePackages = scanPackages;
        } else {
            ComponentScan componentScan = primarySource.getAnnotation(ComponentScan.class);
            if (componentScan != null) {
                basePackages = getComponentScanPackages(componentScan);
            } else {
                basePackages = new String[]{getBasePackage(primarySource)};
            }
        }

        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
        scanner.scan(basePackages);

        logger.info("Scanned {} packages: {}", basePackages.length, Arrays.toString(basePackages));
    }

    protected String[] getComponentScanPackages(ComponentScan componentScan) {
        List<String> packages = new ArrayList<>();
        
        // From value
        packages.addAll(Arrays.asList(componentScan.value()));
        
        // From basePackages
        packages.addAll(Arrays.asList(componentScan.basePackages()));
        
        // From basePackageClasses
        for (Class<?> cls : componentScan.basePackageClasses()) {
            packages.add(cls.getPackage().getName());
        }
        
        if (packages.isEmpty()) {
            packages.add(getBasePackage(primarySource));
        }
        
        return packages.toArray(new String[0]);
    }

    protected void registerWebComponents(GenericApplicationContext context) {
        // Register handler mapping
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping(context);
        context.registerBeanDefinition("requestMappingHandlerMapping",
                createBeanDefinition(RequestMappingHandlerMapping.class, handlerMapping));

        // Register handler adapter
        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        context.registerBeanDefinition("requestMappingHandlerAdapter",
                createBeanDefinition(RequestMappingHandlerAdapter.class, handlerAdapter));

        // Register view resolvers (in priority order: prefix-based first, then template)
        ViewResolver redirectViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("redirect:")) {
                return new RedirectView(viewName.substring(9));
            }
            return null;
        };
        context.registerBeanDefinition("redirectViewResolver",
                createBeanDefinition(ViewResolver.class, redirectViewResolver));

        ViewResolver jsonViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("json:")) {
                return new JsonView();
            }
            return null;
        };
        context.registerBeanDefinition("jsonViewResolver",
                createBeanDefinition(ViewResolver.class, jsonViewResolver));

        // Inline prefix view resolvers for html:/text:/xml:
        ViewResolver htmlViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("html:")) {
                return new HtmlView(viewName.substring(5));
            }
            return null;
        };
        context.registerBeanDefinition("htmlViewResolver",
                createBeanDefinition(ViewResolver.class, htmlViewResolver));

        ViewResolver textViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("text:")) {
                return new TextView(viewName.substring(5));
            }
            return null;
        };
        context.registerBeanDefinition("textViewResolver",
                createBeanDefinition(ViewResolver.class, textViewResolver));

        ViewResolver xmlViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("xml:")) {
                return new XmlView(viewName.substring(4));
            }
            return null;
        };
        context.registerBeanDefinition("xmlViewResolver",
                createBeanDefinition(ViewResolver.class, xmlViewResolver));

        // Template file resolver (falls through for non-prefixed names)
        TemplateViewResolver templateResolver = new TemplateViewResolver(templatePath, ".html");
        context.registerBeanDefinition("templateViewResolver",
                createBeanDefinition(TemplateViewResolver.class, templateResolver));

        // Web event publisher — auto-discovers WebEventListener beans
        WebEventPublisher eventPublisher = new WebEventPublisher(context);
        context.registerBeanDefinition("webEventPublisher",
                createBeanDefinition(WebEventPublisher.class, eventPublisher));

        // Exception handler registry — interface-based, auto-discovers ExceptionHandler beans
        ExceptionHandlerRegistry exceptionRegistry = new ExceptionHandlerRegistry(context);
        context.registerBeanDefinition("exceptionHandlerRegistry",
                createBeanDefinition(ExceptionHandlerRegistry.class, exceptionRegistry));

        // @ControllerAdvice bridge — for annotation-based exception handling
        ExceptionHandlerExceptionResolver adviceResolver =
                new ExceptionHandlerExceptionResolver(context);
        context.registerBeanDefinition("exceptionHandlerExceptionResolver",
                createBeanDefinition(ExceptionHandlerExceptionResolver.class, adviceResolver));
    }

    protected String getBasePackage(Class<?> clazz) {
        return clazz.getPackage().getName();
    }

    protected void registerProgrammaticComponents(GenericApplicationContext context) {
        // Register programmatic exception handlers
        for (ExceptionHandler<?> handler : programmaticExceptionHandlers) {
            String name = "exceptionHandler-" + handler.getClass().getSimpleName();
            context.registerBeanDefinition(name, createBeanDefinition(ExceptionHandler.class, handler));
        }
        // Register programmatic interceptors
        for (int i = 0; i < programmaticInterceptors.size(); i++) {
            String name = "interceptor-" + i;
            context.registerBeanDefinition(name,
                    createBeanDefinition(HandlerInterceptor.class, programmaticInterceptors.get(i)));
        }
        // Register programmatic filters
        for (Filter filter : programmaticFilters) {
            String name = "filter-" + filter.getClass().getSimpleName();
            context.registerBeanDefinition(name, createBeanDefinition(Filter.class, filter));
        }
        // Register programmatic view resolvers
        for (int i = 0; i < programmaticViewResolvers.size(); i++) {
            String name = "viewResolver-" + i;
            context.registerBeanDefinition(name,
                    createBeanDefinition(ViewResolver.class, programmaticViewResolvers.get(i)));
        }
        // Register programmatic response body advices
        for (ResponseBodyAdvice advice : programmaticResponseAdvices) {
            String name = "responseAdvice-" + advice.getClass().getSimpleName();
            context.registerBeanDefinition(name, createBeanDefinition(ResponseBodyAdvice.class, advice));
        }
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

    // --- Programmatic registration API ---

    public void setServerPort(int port) { this.serverPort = port; }
    public void setTemplatePath(String path) { this.templatePath = path; }
    public void setScanAnnotations(boolean scan) { this.scanAnnotations = scan; }
    public void setScanPackages(String[] packages) { this.scanPackages = packages; }

    public void registerExceptionHandler(ExceptionHandler<?> handler) {
        programmaticExceptionHandlers.add(handler);
    }

    public void registerInterceptor(HandlerInterceptor interceptor) {
        programmaticInterceptors.add(interceptor);
    }

    public void registerFilter(Filter filter) {
        programmaticFilters.add(filter);
    }

    public void registerViewResolver(ViewResolver resolver) {
        programmaticViewResolvers.add(resolver);
    }

    public void registerResponseBodyAdvice(ResponseBodyAdvice advice) {
        programmaticResponseAdvices.add(advice);
    }

    // --- Standard setters ---

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

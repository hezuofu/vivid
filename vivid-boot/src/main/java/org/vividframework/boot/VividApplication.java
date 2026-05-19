package org.vividframework.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanDefinitionRegistry;
import org.vividframework.beans.RootBeanDefinition;
import org.vividframework.beans.annotation.Component;
import org.vividframework.beans.annotation.ComponentScan;
import org.vividframework.beans.scanner.ClassPathBeanDefinitionScanner;
import org.vividframework.config.Environment;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.event.ApplicationEventPublisher;
import org.vividframework.server.WebServer;
import org.vividframework.server.ServletContextInitializer;
import org.vividframework.server.netty.NettyServletWebServerFactory;
import org.vividframework.server.servlet.DispatcherAdaptorServlet;
import org.vividframework.web.DispatcherHandler;
import org.vividframework.web.RequestMappingHandlerMapping;
import org.vividframework.web.StaticResourceHandler;
import org.vividframework.web.filter.Filter;
import org.vividframework.web.handler.ExceptionHandler;
import org.vividframework.web.handler.ExceptionHandlerRegistry;
import org.vividframework.web.interceptor.HandlerInterceptor;
import org.vividframework.webmvc.ExceptionHandlerExceptionResolver;
import org.vividframework.webmvc.RequestMappingHandlerAdapter;
import org.vividframework.webmvc.ResponseBodyAdvice;
import org.vividframework.web.resolver.ContentNegotiatingViewResolver;
import org.vividframework.web.resolver.TemplateViewResolver;
import org.vividframework.web.resolver.ViewResolver;
import org.vividframework.web.view.HtmlView;
import org.vividframework.web.view.JsonView;
import org.vividframework.web.view.RedirectView;
import org.vividframework.web.view.TextView;
import org.vividframework.web.view.XmlView;

import java.util.*;

/**
 * Vivid Application entry point — the main bootstrap class.
 *
 * <pre>
 * // Simple
 * VividApplication.run(MyApp.class, args);
 *
 * // Programmatic
 * VividApplication.builder(MyApp.class).port(9090).run(args);
 * </pre>
 *
 * @author sketch
 */
public class VividApplication {

    private static final Logger logger = LoggerFactory.getLogger(VividApplication.class);

    private final Class<?> primarySource;
    private Properties defaultProperties = new Properties();
    private boolean webEnvironment = true;
    private GenericApplicationContext applicationContext;
    private WebServer webServer;
    private String[] args;

    // Configurable settings
    private int serverPort = 8080;
    private String templatePath = "templates/";
    private boolean scanAnnotations = true;
    private String[] scanPackages;
    private final List<ExceptionHandler<?>> programmaticExceptionHandlers = new ArrayList<>();
    private final List<HandlerInterceptor> programmaticInterceptors = new ArrayList<>();
    private final List<Filter> programmaticFilters = new ArrayList<>();
    private final List<ViewResolver> programmaticViewResolvers = new ArrayList<>();
    private final List<ResponseBodyAdvice> programmaticResponseAdvices = new ArrayList<>();

    public VividApplication(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    // --- Static entry points ---

    public static void run(Class<?> primarySource, String... args) {
        new VividApplication(primarySource).run(args);
    }

    public static VividApplicationBuilder builder(Class<?> primarySource) {
        return new VividApplicationBuilder(primarySource);
    }

    // --- Bootstrap ---

    public GenericApplicationContext run(String... args) {
        this.args = args;
        logger.info("Starting Vivid Application...");

        try {
            applicationContext = createApplicationContext();
            configureEnvironment();
            prepareContext(applicationContext);
            applicationContext.refresh();

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
        defaultProperties.forEach((key, value) -> {
            if (env.getProperty(key.toString()) == null) {
                env.setProperty(key.toString(), value.toString());
            }
        });
        if (env.getProperty("server.port") == null) {
            env.setProperty("server.port", String.valueOf(serverPort));
        }
    }

    protected void prepareContext(GenericApplicationContext context) {
        registerWebComponents(context);
        registerDispatcherHandler(context);
        loadAutoConfigurations(context);
        scanAndRegisterBeans(context);
        registerProgrammaticComponents(context);

        ApplicationEventPublisher.SimpleApplicationEventPublisher eventPublisher =
                new ApplicationEventPublisher.SimpleApplicationEventPublisher();
        context.registerBeanDefinition("eventPublisher",
                createBeanDefinition(ApplicationEventPublisher.SimpleApplicationEventPublisher.class, eventPublisher));
    }

    protected void registerWebComponents(GenericApplicationContext context) {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping(context);
        context.registerBeanDefinition("requestMappingHandlerMapping",
                createBeanDefinition(RequestMappingHandlerMapping.class, handlerMapping));

        RequestMappingHandlerAdapter handlerAdapter = new RequestMappingHandlerAdapter();
        context.registerBeanDefinition("requestMappingHandlerAdapter",
                createBeanDefinition(RequestMappingHandlerAdapter.class, handlerAdapter));

        // View resolvers
        ViewResolver redirectViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("redirect:")) {
                return new RedirectView(viewName.substring(9));
            }
            return null;
        };
        context.registerBeanDefinition("redirectViewResolver", createBeanDefinition(ViewResolver.class, redirectViewResolver));

        ViewResolver jsonViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("json:")) return new JsonView();
            return null;
        };
        context.registerBeanDefinition("jsonViewResolver", createBeanDefinition(ViewResolver.class, jsonViewResolver));

        ViewResolver htmlViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("html:")) return new HtmlView(viewName.substring(5));
            return null;
        };
        context.registerBeanDefinition("htmlViewResolver", createBeanDefinition(ViewResolver.class, htmlViewResolver));

        ViewResolver textViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("text:")) return new TextView(viewName.substring(5));
            return null;
        };
        context.registerBeanDefinition("textViewResolver", createBeanDefinition(ViewResolver.class, textViewResolver));

        ViewResolver xmlViewResolver = viewName -> {
            if (viewName != null && viewName.startsWith("xml:")) return new XmlView(viewName.substring(4));
            return null;
        };
        context.registerBeanDefinition("xmlViewResolver", createBeanDefinition(ViewResolver.class, xmlViewResolver));

        StaticResourceHandler staticHandler = new StaticResourceHandler("/static/**", "static/");
        context.registerBeanDefinition("staticResourceHandler", createBeanDefinition(StaticResourceHandler.class, staticHandler));

        ContentNegotiatingViewResolver negotiatingResolver = new ContentNegotiatingViewResolver();
        context.registerBeanDefinition("contentNegotiatingViewResolver",
                createBeanDefinition(ContentNegotiatingViewResolver.class, negotiatingResolver));

        TemplateViewResolver templateResolver = new TemplateViewResolver(templatePath, ".html");
        context.registerBeanDefinition("templateViewResolver", createBeanDefinition(TemplateViewResolver.class, templateResolver));

        org.vividframework.web.event.WebEventPublisher eventPublisher =
                new org.vividframework.web.event.WebEventPublisher(context);
        context.registerBeanDefinition("webEventPublisher",
                createBeanDefinition(org.vividframework.web.event.WebEventPublisher.class, eventPublisher));

        ExceptionHandlerRegistry exceptionRegistry = new ExceptionHandlerRegistry(context);
        context.registerBeanDefinition("exceptionHandlerRegistry",
                createBeanDefinition(ExceptionHandlerRegistry.class, exceptionRegistry));

        ExceptionHandlerExceptionResolver adviceResolver = new ExceptionHandlerExceptionResolver(context);
        context.registerBeanDefinition("exceptionHandlerExceptionResolver",
                createBeanDefinition(ExceptionHandlerExceptionResolver.class, adviceResolver));
    }

    protected void registerDispatcherHandler(GenericApplicationContext context) {
        DispatcherHandler dispatcherHandler = new DispatcherHandler(context);
        context.registerBeanDefinition("dispatcherHandler",
                createBeanDefinition(DispatcherHandler.class, dispatcherHandler));
    }

    protected void loadAutoConfigurations(GenericApplicationContext context) {
        List<String> autoConfigClasses = loadFactoryNames();
        for (String className : autoConfigClasses) {
            try {
                Class<?> configClass = Class.forName(className);
                if (configClass.isAnnotationPresent(Component.class) || configClass.getName().endsWith("Configuration")) {
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
                if (value != null) for (String name : value.split(",")) names.add(name.trim());
            }
        } catch (Exception ignored) {}
        return names;
    }

    protected void scanAndRegisterBeans(GenericApplicationContext context) {
        if (!scanAnnotations) return;
        String[] packages;
        if (scanPackages != null) {
            packages = scanPackages;
        } else {
            ComponentScan cs = primarySource.getAnnotation(ComponentScan.class);
            if (cs != null) packages = getComponentScanPackages(cs);
            else packages = new String[]{getBasePackage(primarySource)};
        }
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(context);
        scanner.scan(packages);
        logger.info("Scanned {} packages: {}", packages.length, Arrays.toString(packages));
    }

    protected String[] getComponentScanPackages(ComponentScan cs) {
        List<String> pkgs = new ArrayList<>();
        pkgs.addAll(Arrays.asList(cs.value()));
        pkgs.addAll(Arrays.asList(cs.basePackages()));
        for (Class<?> cls : cs.basePackageClasses()) pkgs.add(cls.getPackage().getName());
        if (pkgs.isEmpty()) pkgs.add(getBasePackage(primarySource));
        return pkgs.toArray(new String[0]);
    }

    protected void registerProgrammaticComponents(GenericApplicationContext context) {
        for (ExceptionHandler<?> h : programmaticExceptionHandlers)
            context.registerBeanDefinition("eh-" + h.getClass().getSimpleName(),
                    createBeanDefinition(ExceptionHandler.class, h));
        for (int i = 0; i < programmaticInterceptors.size(); i++)
            context.registerBeanDefinition("interceptor-" + i,
                    createBeanDefinition(HandlerInterceptor.class, programmaticInterceptors.get(i)));
        for (Filter f : programmaticFilters)
            context.registerBeanDefinition("filter-" + f.getClass().getSimpleName(), createBeanDefinition(Filter.class, f));
        for (int i = 0; i < programmaticViewResolvers.size(); i++)
            context.registerBeanDefinition("viewResolver-" + i,
                    createBeanDefinition(ViewResolver.class, programmaticViewResolvers.get(i)));
        for (ResponseBodyAdvice a : programmaticResponseAdvices)
            context.registerBeanDefinition("advice-" + a.getClass().getSimpleName(),
                    createBeanDefinition(ResponseBodyAdvice.class, a));
    }

    protected void startWebServer() throws Exception {
        DispatcherHandler dispatcher = applicationContext.getBean("dispatcherHandler", DispatcherHandler.class);

        int port = serverPort;
        Environment env = applicationContext.getEnvironment();
        if (env != null) {
            String ps = env.getProperty("server.port");
            if (ps != null) port = Integer.parseInt(ps);
        }

        NettyServletWebServerFactory factory = new NettyServletWebServerFactory(port);
        DispatcherAdaptorServlet dispatcherServlet = new DispatcherAdaptorServlet(
                request -> dispatcher.handle(request));
        factory.addInitializers(ctx -> ctx.addServlet("dispatcher", dispatcherServlet).addMapping("/"));

        webServer = factory.getWebServer();
        webServer.start();
        logger.info("Web server started on port {}", port);
    }

    protected String getBasePackage(Class<?> clazz) { return clazz.getPackage().getName(); }

    protected RootBeanDefinition createBeanDefinition(Class<?> type, Object instance) {
        RootBeanDefinition def = new RootBeanDefinition(type);
        def.setInstance(instance);
        return def;
    }

    // --- Programmatic registration API ---

    public void setServerPort(int port) { this.serverPort = port; }
    public void setTemplatePath(String path) { this.templatePath = path; }
    public void setScanAnnotations(boolean scan) { this.scanAnnotations = scan; }
    public void setScanPackages(String[] packages) { this.scanPackages = packages; }
    public void registerExceptionHandler(ExceptionHandler<?> h) { programmaticExceptionHandlers.add(h); }
    public void registerInterceptor(HandlerInterceptor i) { programmaticInterceptors.add(i); }
    public void registerFilter(Filter f) { programmaticFilters.add(f); }
    public void registerViewResolver(ViewResolver v) { programmaticViewResolvers.add(v); }
    public void registerResponseBodyAdvice(ResponseBodyAdvice a) { programmaticResponseAdvices.add(a); }

    public void setDefaultProperties(Properties p) { defaultProperties = p; }
    public void setWebEnvironment(boolean w) { webEnvironment = w; }
    public GenericApplicationContext getApplicationContext() { return applicationContext; }
    public WebServer getWebServer() { return webServer; }

    // --- Builder ---

    public static class VividApplicationBuilder {
        private final VividApplication app;
        VividApplicationBuilder(Class<?> source) { this.app = new VividApplication(source); }

        public VividApplicationBuilder port(int port) { app.serverPort = port; return this; }
        public VividApplicationBuilder templatePath(String path) { app.templatePath = path; return this; }
        public VividApplicationBuilder noScan() { app.scanAnnotations = false; return this; }
        public VividApplicationBuilder scanPackages(String... pkgs) { app.scanPackages = pkgs; return this; }
        public VividApplicationBuilder registerExceptionHandler(ExceptionHandler<?> h) { app.registerExceptionHandler(h); return this; }
        public VividApplicationBuilder registerInterceptor(HandlerInterceptor i) { app.registerInterceptor(i); return this; }
        public VividApplicationBuilder registerFilter(Filter f) { app.registerFilter(f); return this; }
        public VividApplicationBuilder registerViewResolver(ViewResolver v) { app.registerViewResolver(v); return this; }
        public VividApplicationBuilder registerResponseBodyAdvice(ResponseBodyAdvice a) { app.registerResponseBodyAdvice(a); return this; }

        public GenericApplicationContext run(String... args) { return app.run(args); }
    }
}

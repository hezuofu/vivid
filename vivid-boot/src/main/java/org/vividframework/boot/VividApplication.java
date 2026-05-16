package org.vividframework.boot;

import org.vividframework.context.GenericApplicationContext;
import org.vividframework.web.DispatcherHandler;
import org.vividframework.web.filter.Filter;
import org.vividframework.web.handler.ExceptionHandler;
import org.vividframework.web.interceptor.HandlerInterceptor;
import org.vividframework.web.resolver.ViewResolver;
import org.vividframework.webmvc.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Vivid Application entry point with fluent builder API.
 *
 * <pre>
 * // Simple
 * VividApplication.run(SampleApplication.class, args);
 *
 * // Programmatic
 * VividApplication.builder(SampleApplication.class)
 *     .port(9090)
 *     .registerExceptionHandler(new MyHandler())
 *     .registerInterceptor(new LoggingInterceptor())
 *     .run(args);
 * </pre>
 *
 * @author sketch
 */
public class VividApplication {

    public static void run(Class<?> primarySource, String... args) {
        new SpringApplication(primarySource).run(args);
    }

    public static VividApplicationBuilder builder(Class<?> primarySource) {
        return new VividApplicationBuilder(primarySource);
    }

    /**
     * Fluent builder for programmatic application assembly.
     * Supports both programmatic registration and annotation scanning.
     */
    public static class VividApplicationBuilder {

        private final Class<?> primarySource;
        private int port = 8080;
        private String templatePath = "templates/";
        private boolean scanAnnotations = true;
        private final List<ExceptionHandler<?>> exceptionHandlers = new ArrayList<>();
        private final List<HandlerInterceptor> interceptors = new ArrayList<>();
        private final List<Filter> filters = new ArrayList<>();
        private final List<ViewResolver> viewResolvers = new ArrayList<>();
        private final List<ResponseBodyAdvice> responseAdvices = new ArrayList<>();
        private String[] scanPackages;

        VividApplicationBuilder(Class<?> primarySource) {
            this.primarySource = primarySource;
        }

        /** Set server port. */
        public VividApplicationBuilder port(int port) {
            this.port = port;
            return this;
        }

        /** Set template file search path. */
        public VividApplicationBuilder templatePath(String path) {
            this.templatePath = path;
            return this;
        }

        /** Disable annotation scanning (pure programmatic mode). */
        public VividApplicationBuilder noScan() {
            this.scanAnnotations = false;
            return this;
        }

        /** Explicit scan packages (overrides annotation-based scanning). */
        public VividApplicationBuilder scanPackages(String... packages) {
            this.scanPackages = packages;
            return this;
        }

        /** Register an exception handler. */
        public VividApplicationBuilder registerExceptionHandler(ExceptionHandler<?> handler) {
            this.exceptionHandlers.add(handler);
            return this;
        }

        /** Register an interceptor. */
        public VividApplicationBuilder registerInterceptor(HandlerInterceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        /** Register a filter. */
        public VividApplicationBuilder registerFilter(Filter filter) {
            this.filters.add(filter);
            return this;
        }

        /** Register a view resolver. */
        public VividApplicationBuilder registerViewResolver(ViewResolver resolver) {
            this.viewResolvers.add(resolver);
            return this;
        }

        /** Register a response body advice. */
        public VividApplicationBuilder registerResponseBodyAdvice(ResponseBodyAdvice advice) {
            this.responseAdvices.add(advice);
            return this;
        }

        /** Build and run the application. */
        public GenericApplicationContext run(String... args) {
            SpringApplication app = new SpringApplication(primarySource);
            applyTo(app);
            return app.run(args);
        }

        /** Apply builder configuration to a SpringApplication. */
        void applyTo(SpringApplication app) {
            app.setServerPort(port);
            app.setTemplatePath(templatePath);
            app.setScanAnnotations(scanAnnotations);

            for (ExceptionHandler<?> h : exceptionHandlers) app.registerExceptionHandler(h);
            for (HandlerInterceptor i : interceptors) app.registerInterceptor(i);
            for (Filter f : filters) app.registerFilter(f);
            for (ViewResolver v : viewResolvers) app.registerViewResolver(v);
            for (ResponseBodyAdvice a : responseAdvices) app.registerResponseBodyAdvice(a);

            if (scanPackages != null) {
                app.setScanPackages(scanPackages);
            }
        }
    }
}

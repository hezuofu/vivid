package org.vividframework.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.http.HttpRequestStreamingHandler;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.StreamingHttpServerResponse;
import org.vividframework.web.filter.Filter;
import org.vividframework.web.filter.FilterChain;
import org.vividframework.web.handler.HandlerAdapter;
import org.vividframework.web.handler.HandlerExecutionChain;
import org.vividframework.web.mapping.HandlerMapping;
import org.vividframework.web.mapping.HandlerMapping.SimpleUrlHandlerMapping;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.resolver.HandlerExceptionResolver;
import org.vividframework.web.resolver.ViewResolver;
import org.vividframework.web.view.View;
import org.vividframework.event.RequestHandledEvent;
import org.vividframework.event.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Front controller orchestrating the full request processing pipeline:
 * filters → handler mapping → interceptors → handler adapter → view rendering → response.
 * Implements StreamingHttpRequestHandler for SSE/file download support.
 * @author Jon Fisher
 */
public class DispatcherHandler implements HttpRequestStreamingHandler {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    private GenericApplicationContext applicationContext;
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private List<ViewResolver> viewResolvers;
    private List<HandlerExceptionResolver> handlerExceptionResolvers;
    private List<Filter> filters;
    private ApplicationEventPublisher eventPublisher;

    public DispatcherHandler() {
        this.handlerMappings = new ArrayList<>();
        this.handlerAdapters = new ArrayList<>();
        this.viewResolvers = new ArrayList<>();
        this.handlerExceptionResolvers = new ArrayList<>();
        this.filters = new ArrayList<>();
    }

    public DispatcherHandler(GenericApplicationContext applicationContext) {
        this();
        this.applicationContext = applicationContext;
        initStrategies();
    }

    public void setApplicationContext(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initStrategies();
    }

    public GenericApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private void initStrategies() {
        if (applicationContext == null) {
            // Add defaults
            if (handlerMappings.isEmpty()) {
                handlerMappings.add(new SimpleUrlHandlerMapping());
            }
            return;
        }

        loadBeansOfType(HandlerMapping.class, handlerMappings);
        loadBeansOfType(HandlerAdapter.class, handlerAdapters);
        loadBeansOfType(ViewResolver.class, viewResolvers);
        loadBeansOfType(HandlerExceptionResolver.class, handlerExceptionResolvers);
        loadBeansOfType(Filter.class, filters);

        if (handlerMappings.isEmpty()) {
            handlerMappings.add(new SimpleUrlHandlerMapping());
        }

        // Sort filters by order
        filters.sort(Comparator.comparingInt(Filter::getOrder));
    }

    @SuppressWarnings("unchecked")
    private <T> void loadBeansOfType(Class<T> type, List<T> target) {
        try {
            Map<String, T> beans = applicationContext.getBeansOfType(type);
            if (beans != null) {
                target.addAll(beans.values());
            }
        } catch (Exception e) {
            logger.debug("No {} beans found in context", type.getSimpleName());
        }
    }

    /**
     * Handle a streaming request. Used for SSE, file downloads, etc.
     */
    @Override
    public void handle(HttpServerRequest request, StreamingHttpServerResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        Exception handlerException = null;
        Object handler = null;
        HandlerExecutionChain executionChain = null;

        try {
            handler = getHandler(request);
            if (handler == null) {
                response.status(404).body("No handler found");
                response.complete();
                return;
            }

            executionChain = getHandlerExecutionChain(handler, request);

            if (!executionChain.applyPreHandle(request)) {
                response.status(403).body("Request blocked by interceptor");
                response.complete();
                return;
            }

            HandlerAdapter adapter = getHandlerAdapter(handler);
            Object result = adapter.handle(request, handler);

            processStreamingResult(result, request, response);

            executionChain.applyPostHandle(request, result);
            executionChain.triggerAfterCompletion(request, null);

        } catch (Exception e) {
            handlerException = e;
            try {
                response.status(500).body("Error: " + e.getMessage());
            } catch (Exception ignored) {}
            response.complete();
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            publishRequestHandledEvent(request, processingTime, handler, handlerException);
        }
    }

    /**
     * Handle a request through the full pipeline (buffered mode).
     */
    public HttpServletResponse handle(HttpServerRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        Exception handlerException = null;
        Object handler = null;
        HandlerExecutionChain executionChain = null;

        try {
            // 1. Get handler
            handler = getHandler(request);
            if (handler == null) {
                return HttpServletResponse.notFound()
                        .mutate().content("No handler found for " + request.getMethod() + " " + request.getPath()).build();
            }

            // 2. Build execution chain with interceptors
            executionChain = getHandlerExecutionChain(handler, request);

            // 3. Execute through filter chain
            if (!filters.isEmpty()) {
                return executeWithFilters(request, executionChain);
            }

            // 4. Execute handler chain directly
            return executeHandlerChain(request, executionChain);

        } catch (Exception e) {
            handlerException = e;
            return handleException(request, e);
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            publishRequestHandledEvent(request, processingTime, handler, handlerException);
        }
    }

    /**
     * Execute handler through the filter chain.
     */
    private HttpServletResponse executeWithFilters(HttpServerRequest request, HandlerExecutionChain chain) {
        FilterChain filterChain = new FilterChain.DefaultFilterChain(filters, (req) -> {
            try {
                return executeHandlerChain(request, chain);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            filterChain.doFilter(request);
            // If filter chain didn't produce a response, return ok
            return HttpServletResponse.ok();
        } catch (Exception e) {
            logger.error("Filter chain error", e);
            return HttpServletResponse.internalServerError()
                    .mutate().content("Filter error: " + e.getMessage()).build();
        }
    }

    /**
     * Execute the handler chain with interceptor lifecycle.
     */
    private HttpServletResponse executeHandlerChain(HttpServerRequest request, HandlerExecutionChain chain)
            throws Exception {
        Object handler = chain.getHandler();

        // Apply preHandle interceptors
        if (!chain.applyPreHandle(request)) {
            return HttpServletResponse.builder().status(403)
                    .content("Request blocked by interceptor").build();
        }

        Object result;
        try {
            // Execute handler via adapter
            HandlerAdapter adapter = getHandlerAdapter(handler);
            result = adapter.handle(request, handler);
        } catch (Exception e) {
            // Trigger afterCompletion on error
            chain.triggerAfterCompletion(request, e);
            throw e;
        }

        // Apply postHandle interceptors
        chain.applyPostHandle(request, result);

        // Process result into response
        HttpServletResponse response = processHandlerResult(result, request);

        // Trigger afterCompletion (success)
        chain.triggerAfterCompletion(request, null);

        return response;
    }

    /**
     * Process handler result with streaming support.
     * If the result is a streaming view, renders it directly to the response.
     */
    protected void processStreamingResult(Object result, HttpServerRequest request,
                                           StreamingHttpServerResponse response) throws Exception {
        if (result instanceof View view && view.isStreaming()) {
            view.renderStreaming(null, request, response);
            return;
        }

        // For buffered views or other results, render via the standard path
        if (result instanceof ModelAndView mav) {
            Object viewObj = mav.getView();
            if (viewObj instanceof View view && view.isStreaming()) {
                view.renderStreaming(mav.getModel(), request, response);
                return;
            }
        }

        // Fall back to buffered rendering
        HttpServletResponse httpResponse = processHandlerResult(result, request);
        response.status(httpResponse.getStatus());
        response.getHeaders().addAll(httpResponse.getHeaders());
        byte[] body = httpResponse.getContent();
        if (body != null && body.length > 0) {
            response.getOutputStream().write(body);
        }
        response.complete();
    }

    /**
     * Process handler execution result into an HttpServletResponse.
     */
    protected HttpServletResponse processHandlerResult(Object result, HttpServerRequest request) throws Exception {
        if (result instanceof HttpServletResponse) {
            return (HttpServletResponse) result;
        }

        if (result instanceof ModelAndView) {
            return renderModelAndView((ModelAndView) result, request);
        }

        // Default: wrap result as plain text or treat as OK
        if (result != null) {
            return HttpServletResponse.ok()
                    .mutate().content(result.toString()).build();
        }
        return HttpServletResponse.ok();
    }

    /**
     * Render a ModelAndView to HttpServletResponse.
     */
    protected HttpServletResponse renderModelAndView(ModelAndView modelAndView, HttpServerRequest request)
            throws Exception {
        if (modelAndView == null) {
            return HttpServletResponse.ok();
        }

        View view = null;

        // Resolve view: try view object first, then view name
        Object viewObj = modelAndView.getView();
        if (viewObj instanceof View) {
            view = (View) viewObj;
        } else if (modelAndView.getViewName() != null) {
            Object resolved = resolveViewName(modelAndView.getViewName());
            if (resolved instanceof View) {
                view = (View) resolved;
            }
        }

        if (view != null) {
            // Render view into a response builder
            HttpServletResponse.Builder builder = HttpServletResponse.builder();
            view.render(modelAndView.getModel(), request, builder);
            return builder.build();
        }

        // No view: return model as JSON-like string
        if (!modelAndView.getModel().isEmpty()) {
            String content = modelAndView.getModel().toString();
            return HttpServletResponse.ok().mutate().content(content).build();
        }

        return HttpServletResponse.ok();
    }

    protected Object getHandler(HttpServerRequest request) throws Exception {
        for (HandlerMapping mapping : handlerMappings) {
            HandlerExecutionChain chain = mapping.getHandler(request);
            if (chain != null) {
                return chain.getHandler();
            }
        }
        return null;
    }

    protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServerRequest request) {
        if (handler instanceof HandlerExecutionChain) {
            return (HandlerExecutionChain) handler;
        }
        return new HandlerExecutionChain(handler);
    }

    protected HandlerAdapter getHandlerAdapter(Object handler) {
        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        throw new IllegalStateException("No suitable handler adapter for " + handler.getClass().getName());
    }

    protected Object resolveViewName(String viewName) throws Exception {
        for (ViewResolver resolver : viewResolvers) {
            View view = resolver.resolveViewName(viewName);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    protected HttpServletResponse handleException(HttpServerRequest request, Exception ex) {
        logger.error("Request processing failed: {}", request.getPath(), ex);
        for (HandlerExceptionResolver resolver : handlerExceptionResolvers) {
            try {
                Object result = resolver.resolveException(request, ex);
                if (result != null) {
                    if (result instanceof HttpServletResponse) {
                        return (HttpServletResponse) result;
                    }
                    if (result instanceof ModelAndView) {
                        try {
                            return renderModelAndView((ModelAndView) result, request);
                        } catch (Exception e) {
                            logger.warn("Failed to render error view", e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception in handler exception resolver", e);
            }
        }
        return HttpServletResponse.internalServerError()
                .mutate().content("Internal Server Error: " + ex.getMessage()).build();
    }

    protected void publishRequestHandledEvent(HttpServerRequest request, long processingTime,
                                              Object handler, Exception exception) {
        if (eventPublisher != null) {
            RequestHandledEvent event = new RequestHandledEvent(
                    this,
                    request.getPath(),
                    request.getMethod().getName(),
                    processingTime,
                    handler,
                    exception != null ? "500" : "200",
                    exception
            );
            eventPublisher.publishEvent(event);
        }
    }

    // --- Setters and adders ---

    public void setHandlerMappings(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    public void addHandlerMapping(HandlerMapping handlerMapping) {
        this.handlerMappings.add(handlerMapping);
    }

    public void setHandlerAdapters(List<HandlerAdapter> handlerAdapters) {
        this.handlerAdapters = handlerAdapters;
    }

    public void addHandlerAdapter(HandlerAdapter handlerAdapter) {
        this.handlerAdapters.add(handlerAdapter);
    }

    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    public void addViewResolver(ViewResolver viewResolver) {
        this.viewResolvers.add(viewResolver);
    }

    public void setHandlerExceptionResolvers(List<HandlerExceptionResolver> handlerExceptionResolvers) {
        this.handlerExceptionResolvers = handlerExceptionResolvers;
    }

    public void addHandlerExceptionResolver(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolvers.add(handlerExceptionResolver);
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}

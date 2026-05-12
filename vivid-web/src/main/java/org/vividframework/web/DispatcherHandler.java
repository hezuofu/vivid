package org.vividframework.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.mapping.HandlerMapping;
import org.vividframework.mapping.HandlerMapping.AbstractHandlerMapping;
import org.vividframework.handler.HandlerAdapter;
import org.vividframework.handler.HandlerExecutionChain;
import org.vividframework.handler.HandlerMethod;
import org.vividframework.mapping.HandlerMapping.SimpleUrlHandlerMapping;
import org.vividframework.model.ModelAndView;
import org.vividframework.resolver.HandlerExceptionResolver;
import org.vividframework.resolver.ViewResolver;
import org.vividframework.event.RequestHandledEvent;
import org.vividframework.event.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Front controller for web requests (similar to DispatcherServlet)
 * @author Jon Fisher
 */
public class DispatcherHandler {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherHandler.class);

    private GenericApplicationContext applicationContext;
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private List<ViewResolver> viewResolvers;
    private List<HandlerExceptionResolver> handlerExceptionResolvers;
    private ApplicationEventPublisher eventPublisher;

    public DispatcherHandler() {
        this.handlerMappings = new ArrayList<>();
        this.handlerAdapters = new ArrayList<>();
        this.viewResolvers = new ArrayList<>();
        this.handlerExceptionResolvers = new ArrayList<>();
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
        if (applicationContext != null) {
            // Get handler mappings from context
            Map<String, HandlerMapping> handlerBeans = null;
            try {
                handlerBeans = applicationContext.getBeansOfType(HandlerMapping.class);
            } catch (Exception e) {
                logger.warn("Could not get handler mappings from context", e);
            }
            if (handlerBeans != null && !handlerBeans.isEmpty()) {
                handlerMappings.addAll(handlerBeans.values());
            }

            // Get handler adapters
            Map<String, HandlerAdapter> adapterBeans = null;
            try {
                adapterBeans = applicationContext.getBeansOfType(HandlerAdapter.class);
            } catch (Exception e) {
                logger.warn("Could not get handler adapters from context", e);
            }
            if (adapterBeans != null && !adapterBeans.isEmpty()) {
                handlerAdapters.addAll(adapterBeans.values());
            }

            // Get view resolvers
            Map<String, ViewResolver> viewBeans = null;
            try {
                viewBeans = applicationContext.getBeansOfType(ViewResolver.class);
            } catch (Exception e) {
                logger.warn("Could not get view resolvers from context", e);
            }
            if (viewBeans != null && !viewBeans.isEmpty()) {
                viewResolvers.addAll(viewBeans.values());
            }
        }

        // Add default mappings if empty
        if (handlerMappings.isEmpty()) {
            handlerMappings.add(new SimpleUrlHandlerMapping());
        }
    }

    public HttpServletResponse handle(HttpServerRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        Exception handlerException = null;
        Object handler = null;
        HttpServletResponse response = null;

        try {
            handler = getHandler(request);
            if (handler == null) {
                return HttpServletResponse.notFound()
                        .mutate().content("No handler found for " + request.getMethod() + " " + request.getPath()).build();
            }

            HandlerExecutionChain chain = getHandlerExecutionChain(handler, request);

            HandlerAdapter adapter = getHandlerAdapter(handler);
            Object result = adapter.handle(request, handler);

            if (result instanceof HttpServletResponse) {
                response = (HttpServletResponse) result;
            } else if (result instanceof ModelAndView) {
                // For ModelAndView, we would typically render the view here
                // For now, return an empty ok response
                response = HttpServletResponse.ok();
            } else {
                response = HttpServletResponse.ok();
            }

            return response;

        } catch (Exception e) {
            handlerException = e;
            return handleException(request, e);
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            publishRequestHandledEvent(request, processingTime, handler, handlerException);
        }
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
        if (handler instanceof HandlerMethod) {
            return new HandlerExecutionChain(handler);
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

    protected void render(ModelAndView modelAndView, HttpServerRequest request) throws Exception {
        if (modelAndView == null) {
            return;
        }

        Object view = modelAndView.getView();
        if (view == null) {
            String viewName = modelAndView.getViewName();
            if (viewName != null) {
                view = resolveViewName(viewName);
            }
        }

        if (view instanceof HttpServletResponse) {
            // Already a response, no need to render
            return;
        }

        if (view != null) {
            org.vividframework.view.View v = (org.vividframework.view.View) view;
            v.render(modelAndView.getModel(), request, (HttpServletResponse) null);
        }
    }

    protected Object resolveViewName(String viewName) throws Exception {
        for (ViewResolver resolver : viewResolvers) {
            org.vividframework.view.View view = resolver.resolveViewName(viewName);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    protected HttpServletResponse handleException(HttpServerRequest request, Exception ex) throws Exception {
        for (HandlerExceptionResolver resolver : handlerExceptionResolvers) {
            try {
                Object result = resolver.resolveException(request, ex);
                if (result != null) {
                    if (result instanceof HttpServletResponse) {
                        return (HttpServletResponse) result;
                    }
                    if (result instanceof ModelAndView) {
                        return HttpServletResponse.internalServerError()
                                .mutate().content(result.toString()).build();
                    }
                }
            } catch (Exception e) {
                logger.warn("Exception in handler exception resolver", e);
            }
        }
        return HttpServletResponse.internalServerError()
                .mutate().content("Error: " + ex.getMessage()).build();
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

    public void setHandlerMappings(List<HandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    public List<HandlerMapping> getHandlerMappings() {
        return handlerMappings;
    }

    public void addHandlerMapping(HandlerMapping handlerMapping) {
        this.handlerMappings.add(handlerMapping);
    }

    public void setHandlerAdapters(List<HandlerAdapter> handlerAdapters) {
        this.handlerAdapters = handlerAdapters;
    }

    public List<HandlerAdapter> getHandlerAdapters() {
        return handlerAdapters;
    }

    public void addHandlerAdapter(HandlerAdapter handlerAdapter) {
        this.handlerAdapters.add(handlerAdapter);
    }

    public void setViewResolvers(List<ViewResolver> viewResolvers) {
        this.viewResolvers = viewResolvers;
    }

    public List<ViewResolver> getViewResolvers() {
        return viewResolvers;
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

    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}

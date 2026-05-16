package org.vividframework.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanFactory;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.resolver.HandlerExceptionResolver;
import org.vividframework.web.view.View;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Registry that discovers ExceptionHandler beans and dispatches exceptions to them.
 * Implements HandlerExceptionResolver so it can be plugged into the DispatcherHandler pipeline.
 *
 * Handlers are auto-discovered from the bean factory, ordered by getOrder(),
 * and the first matching handler processes the exception.
 *
 * @author sketch
 */
public class ExceptionHandlerRegistry implements HandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlerRegistry.class);

    private final List<ExceptionHandler<?>> handlers = new ArrayList<>();
    private BeanFactory beanFactory;

    public ExceptionHandlerRegistry() {}

    public ExceptionHandlerRegistry(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        discover();
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        discover();
    }

    /**
     * Auto-discover all ExceptionHandler beans from the context.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void discover() {
        if (beanFactory == null) return;
        handlers.clear();
        try {
            String[] names = beanFactory.getBeanNamesForType(ExceptionHandler.class);
            for (String name : names) {
                try {
                    Object bean = beanFactory.getBean(name);
                    if (bean instanceof ExceptionHandler handler) {
                        register(handler);
                        logger.debug("Discovered ExceptionHandler: {} for {}",
                                bean.getClass().getSimpleName(),
                                handler.getExceptionType().getSimpleName());
                    }
                } catch (Exception e) {
                    logger.debug("Failed to load ExceptionHandler bean: {}", name);
                }
            }
        } catch (Exception e) {
            logger.debug("No ExceptionHandler beans found");
        }
        // Sort by order
        handlers.sort(Comparator.comparingInt(ExceptionHandler::getOrder));
    }

    /**
     * Register a handler explicitly.
     */
    public <T extends Exception> void register(ExceptionHandler<T> handler) {
        handlers.add(handler);
        handlers.sort(Comparator.comparingInt(ExceptionHandler::getOrder));
    }

    /**
     * Unregister a handler.
     */
    public void unregister(ExceptionHandler<?> handler) {
        handlers.remove(handler);
    }

    /**
     * Get all registered handlers.
     */
    public List<ExceptionHandler<?>> getHandlers() {
        return List.copyOf(handlers);
    }

    @Override
    public ModelAndView resolveException(HttpServerRequest request, Exception exception) {
        for (ExceptionHandler<?> handler : handlers) {
            if (handler.supports(exception)) {
                try {
                    Object result = dispatchException(handler, exception, request);
                    if (result instanceof ModelAndView mav) return mav;
                    if (result instanceof String viewName) return new ModelAndView(viewName);
                    if (result != null) {
                        ModelAndView mav = new ModelAndView();
                        mav.addObject("error", result);
                        return mav;
                    }
                    return null;
                } catch (Exception e) {
                    logger.error("ExceptionHandler {} failed for {}",
                            handler.getClass().getSimpleName(),
                            exception.getClass().getSimpleName(), e);
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Exception> Object dispatchException(ExceptionHandler<T> handler,
                                                            Exception exception,
                                                            HttpServerRequest request) {
        return handler.handle((T) exception, request);
    }
}

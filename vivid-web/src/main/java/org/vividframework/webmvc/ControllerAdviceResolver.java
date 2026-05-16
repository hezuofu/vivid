package org.vividframework.webmvc;

import org.vividframework.beans.BeanFactory;
import org.vividframework.web.handler.HandlerMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolver for @ControllerAdvice beans that provide exception handling,
 * binding, and other cross-cutting concerns
 * @author Jon Fisher
 */
public class ControllerAdviceResolver {

    private final Object bean;
    private final BeanFactory beanFactory;
    private final List<ExceptionHandlerMethodResolver> exceptionResolvers;

    public ControllerAdviceResolver(Object bean, BeanFactory beanFactory) {
        this.bean = bean;
        this.beanFactory = beanFactory;
        this.exceptionResolvers = initExceptionResolvers(bean);
    }

    private List<ExceptionHandlerMethodResolver> initExceptionResolvers(Object bean) {
        List<ExceptionHandlerMethodResolver> resolvers = new ArrayList<>();
        // Scan for @ExceptionHandler methods
        for (java.lang.reflect.Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(org.vividframework.webmvc.annotation.ExceptionHandler.class)) {
                resolvers.add(new ExceptionHandlerMethodResolver(bean, method));
            }
        }
        return resolvers;
    }

    public Object getBean() {
        return bean;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public List<ExceptionHandlerMethodResolver> getExceptionResolvers() {
        return exceptionResolvers;
    }

    /**
     * Resolve an exception to a handler method
     */
    public ExceptionHandlerMethodResolver resolveExceptionMethod(Exception exception) {
        for (ExceptionHandlerMethodResolver resolver : exceptionResolvers) {
            if (resolver.supports(exception)) {
                return resolver;
            }
        }
        return null;
    }

    /**
     * Check if this advice supports the given exception type
     */
    public boolean supportsException(Exception exception) {
        return resolveExceptionMethod(exception) != null;
    }
}

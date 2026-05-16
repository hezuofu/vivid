package org.vividframework.webmvc;

import org.vividframework.http.HttpServerRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Resolver for @ExceptionHandler methods
 * @author Jon Fisher
 */
public class ExceptionHandlerMethodResolver {

    private final Object bean;
    private final Method method;
    private final Class<? extends Exception>[] exceptionTypes;

    @SuppressWarnings("unchecked")
    public ExceptionHandlerMethodResolver(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
        org.vividframework.webmvc.annotation.ExceptionHandler annotation =
            method.getAnnotation(org.vividframework.webmvc.annotation.ExceptionHandler.class);
        if (annotation != null && annotation.value().length > 0) {
            this.exceptionTypes = (Class<? extends Exception>[]) annotation.value();
        } else {
            // Default to first parameter type
            Parameter[] params = method.getParameters();
            if (params.length > 0 && Exception.class.isAssignableFrom(params[0].getType())) {
                this.exceptionTypes = new Class[] { (Class<? extends Exception>) params[0].getType() };
            } else {
                this.exceptionTypes = new Class[] { Exception.class };
            }
        }
    }

    public boolean supports(Exception exception) {
        for (Class<? extends Exception> type : exceptionTypes) {
            if (type.isInstance(exception)) {
                return true;
            }
        }
        return false;
    }

    public Object resolve(HttpServerRequest request, Exception exception) throws Exception {
        method.setAccessible(true);
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            if (HttpServerRequest.class.isAssignableFrom(type)) {
                args[i] = request;
            } else if (Exception.class.isAssignableFrom(type)) {
                args[i] = exception;
            } else {
                args[i] = null;
            }
        }

        return method.invoke(bean, args);
    }

    public Method getMethod() {
        return method;
    }

    public Class<? extends Exception>[] getExceptionTypes() {
        return exceptionTypes;
    }
}

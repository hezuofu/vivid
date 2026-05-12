package org.vividframework.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Invocable handler method that can invoke a method with resolved arguments
 * @author Jon Fisher
 */
public class InvocableHandlerMethod {

    private static final Logger logger = LoggerFactory.getLogger(InvocableHandlerMethod.class);

    private final Object bean;
    private final Method method;

    public InvocableHandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        this(handlerMethod.getBean(), handlerMethod.getMethod());
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * Invoke the method with arguments resolved from the request
     */
    public Object invoke(HttpServerRequest request) throws Exception {
        return invoke(request, null);
    }

    /**
     * Invoke the method with resolved arguments and explicit args
     */
    public Object invoke(HttpServerRequest request, Object... providedArgs) throws Exception {
        Object[] args = getMethodArgumentValues(request, providedArgs);
        return doInvoke(args);
    }

    /**
     * Invoke the method asynchronously
     */
    public CompletableFuture<Object> invokeAsync(HttpServerRequest request) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return invoke(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected Object[] getMethodArgumentValues(HttpServerRequest request, Object... providedArgs) throws Exception {
        HandlerMethod.MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            HandlerMethod.MethodParameter param = parameters[i];

            if (providedArgs != null && providedArgs.length > i) {
                args[i] = providedArgs[i];
            } else {
                args[i] = null;
            }
        }

        return args;
    }

    protected HandlerMethod.MethodParameter[] getMethodParameters() {
        try {
            Method method = getOrderedMethod();
            java.lang.reflect.Parameter[] params = method.getParameters();
            HandlerMethod.MethodParameter[] result = new HandlerMethod.MethodParameter[params.length];
            for (int i = 0; i < params.length; i++) {
                result[i] = new HandlerMethod.MethodParameter(params[i], i);
            }
            return result;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find suitable method", e);
        }
    }

    protected Method getOrderedMethod() throws NoSuchMethodException {
        return method;
    }

    protected Object doInvoke(Object[] args) throws Exception {
        try {
            method.setAccessible(true);
            return method.invoke(bean, args);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access method: " + method, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                throw cause instanceof Exception ? (Exception) cause : new RuntimeException(cause);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if the method returns void
     */
    public boolean isVoidMethod() {
        return method.getReturnType() == void.class || method.getReturnType() == Void.class;
    }

    /**
     * Check if the method returns a future
     */
    public boolean isAsyncMethod() {
        return CompletableFuture.class.isAssignableFrom(method.getReturnType());
    }
}

package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Invocable handler method that resolves arguments via HandlerMethodArgumentResolver.
 * @author sketch
 */
public class InvocableHandlerMethod {

    private final HandlerMethod handlerMethod;
    private HandlerMethodArgumentResolverComposite argumentResolvers;

    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    public void setArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    public Object invoke(HttpServerRequest request) throws Exception {
        Object[] args = getMethodArgumentValues(request);
        return doInvoke(args);
    }

    private Object[] getMethodArgumentValues(HttpServerRequest request) throws Exception {
        HandlerMethod.MethodParameter[] parameters = handlerMethod.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i].getParameter();
            if (argumentResolvers != null && argumentResolvers.supports(handlerMethod, param)) {
                args[i] = argumentResolvers.resolveArgument(handlerMethod, request, param);
            }
        }

        return args;
    }

    private Object doInvoke(Object[] args) throws Exception {
        Method method = handlerMethod.getMethod();
        method.setAccessible(true);
        return method.invoke(handlerMethod.getBean(), args);
    }

    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }
}

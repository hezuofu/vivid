package org.vividframework.webmvc;

import org.vividframework.handler.HandlerMethod;

import java.lang.reflect.Method;

/**
 * Extended InvocableHandlerMethod for webmvc module
 * @author Jon Fisher
 */
public class InvocableHandlerMethod extends org.vividframework.handler.InvocableHandlerMethod {

    public InvocableHandlerMethod(Object bean, Method method) {
        super(bean, method);
    }

    public InvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }

    public void setArgumentResolvers(HandlerMethodArgumentResolverComposite argumentResolvers) {
        // Already handled in parent class
    }
}

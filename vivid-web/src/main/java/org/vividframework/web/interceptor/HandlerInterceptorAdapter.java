package org.vividframework.web.interceptor;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;

/**
 * Abstract adapter for HandlerInterceptor with default implementations
 * @author Jon Fisher
 */
public abstract class HandlerInterceptorAdapter implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServerRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    @Override
    public void postHandle(HttpServerRequest request, HttpServletResponse response, Object handler, Object modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServerRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServerRequest request, HttpServletResponse response, Object handler) {
    }
}

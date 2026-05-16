package org.vividframework.web.interceptor;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;

/**
 * Handler interceptor interface (similar to Spring's HandlerInterceptor)
 * @author Jon Fisher
 */
public interface HandlerInterceptor {

    /**
     * Called before handler execution
     * @return true to continue processing, false to skip handler
     */
    default boolean preHandle(HttpServerRequest request, HttpServletResponse response, Object handler) throws Exception {
        return true;
    }

    /**
     * Called after handler execution but before view rendering
     */
    default void postHandle(HttpServerRequest request, HttpServletResponse response, Object handler, Object modelAndView) throws Exception {
    }

    /**
     * Called after request processing is complete
     */
    default void afterCompletion(HttpServerRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    /**
     * Called when handler starts async processing
     */
    default void afterConcurrentHandlingStarted(HttpServerRequest request, HttpServletResponse response, Object handler) {
    }
}

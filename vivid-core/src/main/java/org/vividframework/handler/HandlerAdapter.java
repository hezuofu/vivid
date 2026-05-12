package org.vividframework.handler;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;

/**
 * Handler adapter interface for executing handlers
 * @author Jon Fisher
 */
public interface HandlerAdapter {

    /**
     * Check if this adapter supports the given handler
     */
    boolean supports(Object handler);

    /**
     * Execute the handler
     * @param request The request
     * @param handler The handler to execute
     * @return The result (can be ModelAndView, HttpServletResponse, or other types)
     * @throws Exception if any error occurs
     */
    Object handle(HttpServerRequest request, Object handler) throws Exception;

    /**
     * Get the order of this adapter
     */
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    interface Ordered {
        int HIGHEST_PRECEDENCE = 0;
        int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    }
}

package org.vividframework.filter;

import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

/**
 * Filter interface for request/response processing chain
 * @author Jon Fisher
 */
public interface Filter {

    /**
     * Filter execution
     * @param request The request
     * @param chain The filter chain
     * @return The response
     * @throws Exception if any error occurs
     */
    HttpServletResponse doFilter(HttpServerRequest request, FilterChain chain) throws Exception;

    /**
     * Get filter name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Get filter order in chain
     */
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Constants for ordering
     */
    interface Ordered {
        int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
        int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    }
}

package org.vividframework.handler;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;

/**
 * Interface for resolving handler method arguments
 * @author Jon Fisher
 */
public interface HandlerMethodArgumentResolver {

    /**
     * Check if this resolver supports the given parameter
     */
    boolean supportsParameter(HandlerMethod.MethodParameter parameter);

    /**
     * Resolve the argument value from the request
     */
    Object resolveArgument(HandlerMethod.MethodParameter parameter, HttpServerRequest request) throws Exception;
}

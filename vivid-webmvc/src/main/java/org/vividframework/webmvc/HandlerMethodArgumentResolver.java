package org.vividframework.webmvc;

import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.http.server.HttpServerResponse;
import org.vividframework.handler.HandlerMethod;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Strategy interface for resolving method argument values
 * @author Jon Fisher
 */
public interface HandlerMethodArgumentResolver {

    /**
     * Check if this resolver supports the given parameter
     */
    boolean supports(HandlerMethod handlerMethod, Parameter parameter);

    /**
     * Resolve the argument value from the request
     */
    Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                          Parameter parameter) throws Exception;
}

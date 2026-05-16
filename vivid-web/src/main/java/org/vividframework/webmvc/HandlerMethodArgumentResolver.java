package org.vividframework.webmvc;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.handler.HandlerMethod;

import java.lang.reflect.Parameter;
import java.util.List;

/**
 * Strategy interface for resolving method argument values
 * @author sketch
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

package org.vividframework.webmvc;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.handler.HandlerMethod;

/**
 * Handler method return value handler interface
 * @author sketch
 */
public interface HandlerMethodReturnValueHandler {

    /**
     * Check if this handler supports the given return type
     */
    boolean supports(HandlerMethod handlerMethod, Class<?> returnType);

    /**
     * Handle the return value
     */
    void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                          HttpServerResponse response, Object returnValue) throws Exception;
}

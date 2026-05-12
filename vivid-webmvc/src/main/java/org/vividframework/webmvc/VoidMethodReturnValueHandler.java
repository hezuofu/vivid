package org.vividframework.webmvc;

import org.vividframework.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.http.server.HttpServerResponse;

/**
 * Handler method return value handler for void return types
 * @author Jon Fisher
 */
public class VoidMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Class<?> returnType) {
        return void.class.equals(returnType) || Void.class.equals(returnType);
    }

    @Override
    public void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                                  HttpServerResponse response, Object returnValue) throws Exception {
        // No content to write for void methods
        response.status(HttpServletResponse.ok().getStatus());
    }
}

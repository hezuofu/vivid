package org.vividframework.samples;

import org.vividframework.beans.annotation.Component;
import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.annotation.ResponseBody;
import org.vividframework.webmvc.ResponseBodyAdvice;

import java.util.Map;

/**
 * Wraps all @ResponseBody API responses in a standard envelope:
 * { "code": 200, "data": ..., "message": "ok", "timestamp": ... }
 */
@Component
public class ApiResponseAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Class<?> returnType) {
        // Apply to all methods, or check for specific annotations/markers
        return handlerMethod != null
                && handlerMethod.hasMethodAnnotation(ResponseBody.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, HandlerMethod handlerMethod, HttpServerRequest request) {
        return Map.of(
                "code", 200,
                "message", "ok",
                "data", body != null ? body : "",
                "timestamp", System.currentTimeMillis()
        );
    }
}

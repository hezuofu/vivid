package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;

/**
 * Intercepts @ResponseBody return values before serialization.
 * Register beans implementing this interface to wrap or transform API responses
 * (e.g. standard envelope: {code, message, data}).
 *
 * <pre>
 * &#64;Component
 * public class ApiResponseAdvice implements ResponseBodyAdvice {
 *     public boolean supports(HandlerMethod method, Class<?> returnType) {
 *         return true; // apply to all @ResponseBody methods
 *     }
 *
 *     public Object beforeBodyWrite(Object body, HandlerMethod method, HttpServerRequest request) {
 *         return Map.of("code", 200, "data", body, "message", "ok");
 *     }
 * }
 * </pre>
 *
 * @author sketch
 */
public interface ResponseBodyAdvice {

    /**
     * Whether this advice applies to the given method and return type.
     */
    boolean supports(HandlerMethod handlerMethod, Class<?> returnType);

    /**
     * Transform the body before writing to the response.
     * @param body the original return value (may be null)
     * @param handlerMethod the handler method being invoked
     * @param request the current request
     * @return the transformed body (can be the original value unchanged)
     */
    Object beforeBodyWrite(Object body, HandlerMethod handlerMethod, HttpServerRequest request);
}

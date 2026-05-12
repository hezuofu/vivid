package org.vividframework.webmvc;

import org.vividframework.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.http.server.HttpServerResponse;
import org.vividframework.web.annotation.RequestBody;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Resolver for @RequestBody annotated method parameters
 * @author Jon Fisher
 */
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, java.lang.reflect.Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   java.lang.reflect.Parameter parameter) throws Exception {
        Class<?> parameterType = parameter.getType();
        byte[] content = request.getContent();

        if (content == null || content.length == 0) {
            return null;
        }

        String body = new String(content, StandardCharsets.UTF_8);
        return convertToType(body, parameterType);
    }

    private Object convertToType(String body, Class<?> parameterType) {
        if (parameterType == String.class) {
            return body;
        } else if (parameterType == byte[].class) {
            return body.getBytes(StandardCharsets.UTF_8);
        } else if (parameterType == int.class || parameterType == Integer.class) {
            return Integer.parseInt(body.trim());
        } else if (parameterType == long.class || parameterType == Long.class) {
            return Long.parseLong(body.trim());
        } else if (parameterType == double.class || parameterType == Double.class) {
            return Double.parseDouble(body.trim());
        } else if (parameterType == boolean.class || parameterType == Boolean.class) {
            return Boolean.parseBoolean(body.trim());
        }
        // For complex types, assume JSON and return as string for now
        // In a full implementation, this would use Jackson ObjectMapper
        return body;
    }
}

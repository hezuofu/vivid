package org.vividframework.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.annotation.RequestBody;

import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;

/**
 * Resolver for @RequestBody annotated method parameters.
 * Uses Jackson for JSON deserialization.
 * @author sketch
 */
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        Class<?> parameterType = parameter.getType();
        byte[] content = request.getContent();

        if (content == null || content.length == 0) {
            return null;
        }

        if (parameterType == String.class) {
            return new String(content, StandardCharsets.UTF_8);
        }
        if (parameterType == byte[].class) {
            return content;
        }

        // Use Jackson for complex types
        return objectMapper.readValue(content, parameterType);
    }
}

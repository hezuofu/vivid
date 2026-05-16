package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.webmvc.annotation.RequestHeader;

import java.lang.reflect.Parameter;

/**
 * Resolver for @RequestHeader annotated method parameters
 * @author sketch
 */
public class RequestHeaderMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return parameter.isAnnotationPresent(RequestHeader.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
        String headerName = requestHeader.value();

        if (headerName.isEmpty()) {
            headerName = parameter.getName();
        }

        HttpHeaders headers = request.getHeaders();
        String value = headers.getFirst(headerName);

        if (value == null || value.isEmpty()) {
            if (requestHeader.required()) {
                throw new IllegalStateException("Required header '" + headerName + "' not found");
            }
            return requestHeader.defaultValue().isEmpty() ? null : requestHeader.defaultValue();
        }

        Class<?> parameterType = parameter.getType();
        return convertValue(value, parameterType);
    }

    private Object convertValue(String value, Class<?> parameterType) {
        if (value == null) {
            return null;
        }
        if (parameterType == String.class) {
            return value;
        } else if (parameterType == int.class || parameterType == Integer.class) {
            return Integer.parseInt(value);
        } else if (parameterType == long.class || parameterType == Long.class) {
            return Long.parseLong(value);
        } else if (parameterType == double.class || parameterType == Double.class) {
            return Double.parseDouble(value);
        } else if (parameterType == boolean.class || parameterType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }
}

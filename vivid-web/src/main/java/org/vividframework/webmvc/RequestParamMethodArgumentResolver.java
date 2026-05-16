package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.annotation.RequestParam;

import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Resolver for @RequestParam annotated method parameters
 * @author Jon Fisher
 */
public class RequestParamMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return parameter.isAnnotationPresent(RequestParam.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        String paramName = requestParam.value();

        if (paramName.isEmpty()) {
            paramName = parameter.getName();
        }

        Map<String, String[]> queryParams = request.getQueryParameters();
        String[] values = queryParams.get(paramName);

        if (values == null || values.length == 0) {
            if (requestParam.required()) {
                throw new IllegalStateException("Required parameter '" + paramName + "' not found");
            }
            return parameter.getType().isPrimitive() ? getDefaultValue(parameter.getType()) : null;
        }

        Class<?> parameterType = parameter.getType();
        return convertValue(values[0], parameterType);
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
        } else if (parameterType == float.class || parameterType == Float.class) {
            return Float.parseFloat(value);
        } else if (parameterType == boolean.class || parameterType == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0.0f;
        if (type == boolean.class) return false;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return null;
    }
}

package org.vividframework.webmvc;

import org.vividframework.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.http.server.HttpServerResponse;
import org.vividframework.web.annotation.PathVariable;

import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Resolver for @PathVariable annotated method parameters
 * @author Jon Fisher
 */
public class PathVariableMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return parameter.isAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
        String variableName = pathVar.value();

        if (variableName.isEmpty()) {
            variableName = parameter.getName();
        }

        Map<String, String> pathVariables = request.getPathVariables();
        String value = pathVariables.get(variableName);

        if (value == null) {
            throw new IllegalStateException("Path variable '" + variableName + "' not found");
        }

        Class<?> parameterType = parameter.getType();
        return convertValue(value, parameterType);
    }

    private Object convertValue(String value, Class<?> parameterType) {
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
        } else if (parameterType == short.class || parameterType == Short.class) {
            return Short.parseShort(value);
        } else if (parameterType == byte.class || parameterType == Byte.class) {
            return Byte.parseByte(value);
        }
        return value;
    }
}

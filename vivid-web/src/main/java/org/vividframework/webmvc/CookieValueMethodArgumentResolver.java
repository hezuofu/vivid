package org.vividframework.webmvc;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.annotation.CookieValue;
import org.vividframework.web.handler.HandlerMethod;

import java.lang.reflect.Parameter;

/**
 * Resolves @CookieValue annotated parameters from request cookies.
 * @author sketch
 */
public class CookieValueMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return parameter.isAnnotationPresent(CookieValue.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        CookieValue ann = parameter.getAnnotation(CookieValue.class);
        String name = ann.value().isEmpty() ? parameter.getName() : ann.value();

        String value = null;
        var cookies = request.getCookies();
        if (cookies.containsKey(name)) {
            value = cookies.get(name).getValue();
        }

        if (value == null) {
            if (!ann.defaultValue().isEmpty()) return convert(ann.defaultValue(), parameter.getType());
            if (ann.required()) throw new IllegalStateException("Required cookie '" + name + "' not found");
            return null;
        }

        return convert(value, parameter.getType());
    }

    private Object convert(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        return value;
    }
}

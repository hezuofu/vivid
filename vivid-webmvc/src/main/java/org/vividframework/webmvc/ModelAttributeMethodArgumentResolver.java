package org.vividframework.webmvc;

import org.vividframework.handler.HandlerMethod;
import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.web.annotation.RequestParam;

import java.lang.reflect.Parameter;
import java.util.Map;

/**
 * Resolver for @ModelAttribute annotated method parameters or for binding request parameters
 * @author Jon Fisher
 */
public class ModelAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        // Support @RequestParam parameters
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

        Map<String, String[]> params = request.getQueryParameters();
        String[] values = params.get(paramName);

        if (values == null || values.length == 0) {
            if (requestParam.required()) {
                throw new IllegalStateException("Required parameter '" + paramName + "' not found");
            }
            return null;
        }

        return values[0];
    }
}

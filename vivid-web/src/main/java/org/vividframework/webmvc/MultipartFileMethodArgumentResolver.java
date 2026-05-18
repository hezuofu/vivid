package org.vividframework.webmvc;

import org.vividframework.http.*;
import org.vividframework.web.annotation.RequestParam;
import org.vividframework.web.handler.HandlerMethod;

import java.lang.reflect.Parameter;

/**
 * Resolves @RequestParam parameters of type MultipartFile or List&lt;MultipartFile&gt;.
 * @author sketch
 */
public class MultipartFileMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        if (!parameter.isAnnotationPresent(RequestParam.class)) return false;
        Class<?> type = parameter.getType();
        return MultipartFile.class.isAssignableFrom(type)
                || (type == java.util.List.class && isMultipartFileList(parameter));
    }

    private boolean isMultipartFileList(Parameter parameter) {
        // Check if it's a List<MultipartFile> - best-effort
        java.lang.reflect.ParameterizedType pt =
                (java.lang.reflect.ParameterizedType) parameter.getParameterizedType();
        if (pt != null && pt.getActualTypeArguments().length > 0) {
            return MultipartFile.class == pt.getActualTypeArguments()[0];
        }
        return false;
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        if (!isMultipart(request)) return null;

        RequestParam reqParam = parameter.getAnnotation(RequestParam.class);
        String name = reqParam.value().isEmpty() ? parameter.getName() : reqParam.value();

        byte[] body = request.getContent();
        String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
        MultipartRequest multipart = MultipartRequest.parse(contentType, body);

        if (parameter.getType() == java.util.List.class) {
            return multipart.getFiles(name);
        }
        return multipart.getFile(name);
    }

    private boolean isMultipart(HttpServerRequest request) {
        String ct = request.getHeader(HttpHeaders.CONTENT_TYPE);
        return ct != null && ct.startsWith("multipart/form-data");
    }
}

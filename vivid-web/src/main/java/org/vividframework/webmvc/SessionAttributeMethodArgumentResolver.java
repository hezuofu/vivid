package org.vividframework.webmvc;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.annotation.SessionAttribute;
import org.vividframework.web.handler.HandlerMethod;

import java.lang.reflect.Parameter;

/**
 * Resolves @SessionAttribute annotated parameters from the HTTP session.
 * @author sketch
 */
public class SessionAttributeMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Parameter parameter) {
        return parameter.isAnnotationPresent(SessionAttribute.class);
    }

    @Override
    public Object resolveArgument(HandlerMethod handlerMethod, HttpServerRequest request,
                                   Parameter parameter) throws Exception {
        SessionAttribute ann = parameter.getAnnotation(SessionAttribute.class);
        String name = ann.value().isEmpty() ? parameter.getName() : ann.value();

        // Session is accessed via request attributes (set by NettyHttpServer)
        // In full servlet mode, request.getAttribute("session") returns the HttpSession
        // We look for session-scoped attributes directly

        Object value = request.getAttribute(name);
        if (value == null && ann.required()) {
            throw new IllegalStateException("Required session attribute '" + name + "' not found");
        }
        return value;
    }
}

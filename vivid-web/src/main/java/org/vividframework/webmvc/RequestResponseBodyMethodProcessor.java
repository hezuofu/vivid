package org.vividframework.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler method return value handler for @ResponseBody annotated methods.
 * Uses Jackson for JSON serialization, with optional ResponseBodyAdvice interception.
 * @author sketch
 */
public class RequestResponseBodyMethodProcessor implements HandlerMethodReturnValueHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<ResponseBodyAdvice> advices = new ArrayList<>();

    public void addAdvice(ResponseBodyAdvice advice) {
        advices.add(advice);
    }

    public void setAdvices(List<ResponseBodyAdvice> advices) {
        this.advices.clear();
        if (advices != null) {
            this.advices.addAll(advices);
        }
    }

    @Override
    public boolean supports(HandlerMethod handlerMethod, Class<?> returnType) {
        if (handlerMethod == null) {
            return false;
        }
        return handlerMethod.hasMethodAnnotation(ResponseBody.class) ||
               handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class);
    }

    @Override
    public void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                                  HttpServerResponse response, Object returnValue) throws Exception {
        // Apply ResponseBodyAdvice before writing
        if (returnValue != null) {
            returnValue = applyAdvices(returnValue, handlerMethod, request);
        }

        if (returnValue == null) {
            return;
        }

        if (returnValue instanceof String || returnValue instanceof CharSequence) {
            response.body(returnValue.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else if (returnValue instanceof byte[]) {
            response.body((byte[]) returnValue);
        } else if (returnValue instanceof HttpServletResponse hr) {
            response.body(hr.getContent());
        } else {
            response.body(objectMapper.writeValueAsBytes(returnValue));
        }
    }

    private Object applyAdvices(Object body, HandlerMethod handlerMethod, HttpServerRequest request) {
        for (ResponseBodyAdvice advice : advices) {
            if (advice.supports(handlerMethod, body != null ? body.getClass() : void.class)) {
                body = advice.beforeBodyWrite(body, handlerMethod, request);
            }
        }
        return body;
    }
}

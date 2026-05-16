package org.vividframework.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.annotation.ResponseBody;

/**
 * Handler method return value handler for @ResponseBody annotated methods.
 * Uses Jackson for JSON serialization.
 * @author Jon Fisher
 */
public class RequestResponseBodyMethodProcessor implements HandlerMethodReturnValueHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (returnValue == null) {
            return;
        }

        if (returnValue instanceof String || returnValue instanceof CharSequence) {
            String text = returnValue.toString();
            response.body(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } else if (returnValue instanceof byte[]) {
            response.body((byte[]) returnValue);
        } else if (returnValue instanceof HttpServletResponse) {
            HttpServletResponse r = (HttpServletResponse) returnValue;
            response.body(r.getContent());
        } else {
            byte[] json = objectMapper.writeValueAsBytes(returnValue);
            response.body(json);
        }
    }
}

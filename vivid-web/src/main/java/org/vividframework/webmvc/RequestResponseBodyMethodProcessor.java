package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.annotation.ResponseBody;

/**
 * Handler method return value handler for @ResponseBody annotated methods
 * @author Jon Fisher
 */
public class RequestResponseBodyMethodProcessor implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Class<?> returnType) {
        return handlerMethod.hasMethodAnnotation(ResponseBody.class) ||
               handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class);
    }

    @Override
    public void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                                  HttpServerResponse response, Object returnValue) throws Exception {
        if (returnValue == null) {
            return;
        }

        HttpServletResponse.Builder builder = HttpServletResponse.builder();

        if (returnValue instanceof String) {
            builder.text((String) returnValue);
        } else if (returnValue instanceof HttpServletResponse) {
            HttpServletResponse r = (HttpServletResponse) returnValue;
            builder.status(r.getStatus())
                   .headers(r.getHeaders())
                   .content(r.getContent());
        } else if (returnValue instanceof CharSequence) {
            builder.text(returnValue.toString());
        } else {
            builder.json(returnValue.toString());
        }

        response.body(builder.build().getContent());
    }
}

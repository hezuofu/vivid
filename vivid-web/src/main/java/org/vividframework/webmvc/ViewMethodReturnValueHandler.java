package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.model.ModelAndView;
import org.vividframework.web.view.View;

/**
 * Handler method return value handler for View-based return types
 * @author sketch
 */
public class ViewMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    @Override
    public boolean supports(HandlerMethod handlerMethod, Class<?> returnType) {
        return View.class.isAssignableFrom(returnType) ||
               ModelAndView.class.isAssignableFrom(returnType);
    }

    @Override
    public void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                                 HttpServerResponse response, Object returnValue) throws Exception {
        if (returnValue instanceof View) {
            View view = (View) returnValue;
            view.render(null, request, response);
        } else if (returnValue instanceof ModelAndView) {
            ModelAndView mav = (ModelAndView) returnValue;
            Object view = mav.getView();
            if (view instanceof View) {
                ((View) view).render(mav.getModel(), request, response);
            } else if (mav.getViewName() != null) {
                response.header("X-View-Name", mav.getViewName());
            }
        }
    }
}

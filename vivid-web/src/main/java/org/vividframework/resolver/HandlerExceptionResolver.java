package org.vividframework.web.resolver;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.model.ModelAndView;

/**
 * Handler exception resolver interface
 * @author Jon Fisher
 */
public interface HandlerExceptionResolver {

    /**
     * Resolve exception
     */
    ModelAndView resolveException(HttpServerRequest request, Exception ex);
}

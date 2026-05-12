package org.vividframework.resolver;

import org.vividframework.http.server.HttpServerRequest;
import org.vividframework.model.ModelAndView;

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

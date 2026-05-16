package org.vividframework.web.resolver;

import org.vividframework.web.view.View;

/**
 * View resolver interface
 * @author Jon Fisher
 */
public interface ViewResolver {

    /**
     * Resolve view name to view
     */
    View resolveViewName(String viewName) throws Exception;
}

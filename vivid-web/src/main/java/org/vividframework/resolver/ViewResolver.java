package org.vividframework.resolver;

import org.vividframework.view.View;

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

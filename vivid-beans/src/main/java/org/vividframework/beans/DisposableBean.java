package org.vividframework.beans;

/**
 * Interface for beans that need to be destroyed
 * @author Jon Fisher
 */
public interface DisposableBean {

    /**
     * Called when bean is destroyed
     */
    void destroy() throws Exception;
}

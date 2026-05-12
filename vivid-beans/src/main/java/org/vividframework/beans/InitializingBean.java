package org.vividframework.beans;

/**
 * Interface for beans that need to react once their properties are set
 * @author Jon Fisher
 */
public interface InitializingBean {

    /**
     * Called after bean properties are set
     */
    void afterPropertiesSet() throws Exception;
}

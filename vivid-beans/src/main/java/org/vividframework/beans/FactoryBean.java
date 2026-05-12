package org.vividframework.beans;

/**
 * Factory bean interface
 * @author Jon Fisher
 */
public interface FactoryBean<T> {

    /**
     * Get the object produced by this factory
     */
    T getObject() throws Exception;

    /**
     * Get the type of object produced
     */
    Class<?> getObjectType();

    /**
     * Is singleton?
     */
    default boolean isSingleton() {
        return true;
    }
}

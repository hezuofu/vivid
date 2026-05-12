package org.vividframework.beans;

import java.util.function.Supplier;

/**
 * Bean factory interface
 * @author Jon Fisher
 */
public interface BeanFactory {

    /**
     * Get bean by name
     */
    Object getBean(String name) throws Exception;

    /**
     * Get bean by type
     */
    <T> T getBean(Class<T> type) throws Exception;

    /**
     * Get bean by name and type
     */
    <T> T getBean(String name, Class<T> type) throws Exception;

    /**
     * Get bean by supplier
     */
    <T> T getBean(Supplier<T> supplier) throws Exception;

    /**
     * Check if contains bean
     */
    boolean containsBean(String name);

    /**
     * Check if bean is singleton
     */
    boolean isSingleton(String name) throws Exception;

    /**
     * Check if bean is prototype
     */
    boolean isPrototype(String name) throws Exception;

    /**
     * Get bean type
     */
    Class<?> getType(String name) throws Exception;
}

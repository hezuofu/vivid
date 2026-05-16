package org.vividframework.beans;

import java.util.Map;

/**
 * Listable bean factory interface
 * @author sketch
 */
public interface ListableBeanFactory extends BeanFactory {

    /**
     * Get all bean names
     */
    String[] getBeanDefinitionNames();

    /**
     * Get bean names by type
     */
    String[] getBeanNamesForType(Class<?> type);

    /**
     * Get beans by type
     */
    <T> Map<String, T> getBeansOfType(Class<T> type) throws Exception;

    /**
     * Get bean names for annotation
     */
    String[] getBeanNamesForAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType);

    /**
     * Find beans with annotation
     */
    Map<String, Object> getBeansWithAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) throws Exception;

    /**
     * Check if bean definition exists
     */
    boolean containsBeanDefinition(String name);

    /**
     * Get bean definition count
     */
    int getBeanDefinitionCount();

    /**
     * Get all bean names matching given type
     */
    String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);
}

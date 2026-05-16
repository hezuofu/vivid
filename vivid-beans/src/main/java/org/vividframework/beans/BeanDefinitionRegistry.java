package org.vividframework.beans;

/**
 * Bean definition registry interface
 * @author sketch
 */
public interface BeanDefinitionRegistry {

    /**
     * Register a bean definition
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    /**
     * Remove a bean definition
     */
    void removeBeanDefinition(String beanName);

    /**
     * Get bean definition
     */
    RootBeanDefinition getBeanDefinition(String beanName);

    /**
     * Check if contains bean definition
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * Get all bean definition names
     */
    String[] getBeanDefinitionNames();

    /**
     * Get bean definition count
     */
    int getBeanDefinitionCount();
}

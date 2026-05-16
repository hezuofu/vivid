package org.vividframework.beans;

/**
 * BeanFactoryPostProcessor allows modification of bean definitions
 * before any beans are instantiated.
 * @author Jon Fisher
 */
@FunctionalInterface
public interface BeanFactoryPostProcessor {

    /**
     * Modify the bean factory's internal state after its standard initialization.
     * Called during the refresh phase, before bean instantiation.
     */
    void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) throws Exception;
}

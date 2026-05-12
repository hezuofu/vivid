package org.vividframework.beans;

/**
 * Bean post processor interface for post-processing beans
 * @author Jon Fisher
 */
public interface BeanPostProcessor {

    /**
     * Set the bean factory (called by container)
     */
    default void setBeanFactory(BeanFactory beanFactory) {
    }

    /**
     * Post-process before initialization
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    /**
     * Post-process after initialization
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        return bean;
    }

    /**
     * Post-process for early exposure of beans
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName, Object earlySingleton) throws Exception {
        return postProcessBeforeInitialization(bean, beanName);
    }
}

package org.vividframework.beans;

/**
 * Bean definition interface
 * @author sketch
 */
public interface BeanDefinition {

    String getBeanClassName();

    Class<?> getBeanClass();

    String getFactoryMethodName();

    String getFactoryBeanName();

    String getScope();

    boolean isSingleton();

    boolean isPrototype();

    boolean isAbstract();

    int getRole();

    int getOrder();

    Object getPrimary();

    boolean isLazyInit();

    String[] getDependsOn();

    boolean hasDependsOn();

    String getDescription();

    /**
     * Role constants
     */
    int ROLE_APPLICATION = 0;
    int ROLE_SUPPORT = 1;
    int ROLE_INFRASTRUCTURE = 2;
}

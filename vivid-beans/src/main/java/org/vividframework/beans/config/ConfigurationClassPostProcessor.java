package org.vividframework.beans.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.*;
import org.vividframework.beans.annotation.Bean;
import org.vividframework.beans.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * BeanFactoryPostProcessor that processes @Configuration classes.
 * Registers bean definitions for each @Bean method found.
 *
 * @author sketch
 */
public class ConfigurationClassPostProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationClassPostProcessor.class);

    @Override
    public void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) throws Exception {
        List<String> configBeanNames = new ArrayList<>();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            RootBeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            Class<?> beanClass = resolveClass(bd, beanFactory);
            if (beanClass != null && beanClass.isAnnotationPresent(Configuration.class)) {
                configBeanNames.add(beanName);
                processConfigurationClass(beanFactory, beanClass, beanName);
            }
        }
        logger.debug("Processed {} @Configuration classes", configBeanNames.size());
    }

    private void processConfigurationClass(DefaultListableBeanFactory beanFactory,
                                            Class<?> configClass, String configBeanName) {
        for (Method method : configClass.getDeclaredMethods()) {
            Bean beanAnn = method.getAnnotation(Bean.class);
            if (beanAnn == null) continue;

            String beanName = beanAnn.value().isEmpty()
                    ? method.getName()
                    : beanAnn.value();

            Class<?> returnType = method.getReturnType();
            if (returnType == void.class || returnType == Void.class) {
                logger.warn("@Bean method {} returns void, skipping", method.getName());
                continue;
            }

            // Register a FactoryBean that invokes this @Bean method
            BeanMethodFactoryBean factoryBean = new BeanMethodFactoryBean(
                    configBeanName, method.getName());
            RootBeanDefinition beanDef = new RootBeanDefinition(BeanMethodFactoryBean.class);
            beanDef.setInstance(factoryBean);
            beanDef.setScope(RootBeanDefinition.SCOPE_SINGLETON);

            beanFactory.registerBeanDefinition(beanName, beanDef);
            logger.debug("Registered @Bean '{}' from {}", beanName, configClass.getSimpleName());
        }
    }

    private Class<?> resolveClass(RootBeanDefinition bd, DefaultListableBeanFactory beanFactory) {
        if (bd.getBeanClass() != null) return bd.getBeanClass();
        if (bd.getBeanClassName() == null) return null;
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(bd.getBeanClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * FactoryBean that invokes a @Bean method on a @Configuration instance.
     */
    public static class BeanMethodFactoryBean implements FactoryBean<Object> {

        private final String configBeanName;
        private final String methodName;

        public BeanMethodFactoryBean(String configBeanName, String methodName) {
            this.configBeanName = configBeanName;
            this.methodName = methodName;
        }

        @Override
        public Object getObject() throws Exception {
            throw new IllegalStateException(
                    "BeanMethodFactoryBean requires createBean(beanFactory) to resolve "
                    + configBeanName + "." + methodName);
        }

        @Override
        public Class<?> getObjectType() {
            return Object.class;
        }

        public Object createBean(DefaultListableBeanFactory beanFactory) throws Exception {
            Object configInstance = beanFactory.getBean(configBeanName);
            Method method = configInstance.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(configInstance);
        }
    }
}

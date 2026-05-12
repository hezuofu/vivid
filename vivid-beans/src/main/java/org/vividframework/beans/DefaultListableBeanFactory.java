package org.vividframework.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.util.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default listable bean factory implementation
 * @author Jon Fisher
 */
public class DefaultListableBeanFactory implements ListableBeanFactory, BeanDefinitionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DefaultListableBeanFactory.class);

    private final Map<String, RootBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> beanNameByType = new ConcurrentHashMap<>();
    private final Set<String> singletonsCurrentlyInCreation = ConcurrentHashMap.newKeySet();
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();
    private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();

    public boolean isAllowCircularReferences() {
        return allowCircularReferences;
    }

    public void setAllowCircularReferences(boolean allowCircularReferences) {
        this.allowCircularReferences = allowCircularReferences;
    }

    private boolean allowCircularReferences = true;

    public void setCurrentlyRefreshsing(boolean currentlyRefreshsing) {
        this.currentlyRefreshsing = currentlyRefreshsing;
    }

    private boolean currentlyRefreshsing = false;

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        Assert.notEmpty(beanName, "Bean name must not be empty");
        Assert.notNull(beanDefinition, "Bean definition must not be null");

        RootBeanDefinition existingDefinition = beanDefinitionMap.get(beanName);
        if (existingDefinition != null) {
            if (existingDefinition.isAbstract()) {
                throw new IllegalStateException("Cannot register bean definition for '" + beanName + "': already exists as abstract");
            }
            logger.info("Replacing bean definition '{}' with new definition", beanName);
        }

        RootBeanDefinition rbd = toRootBeanDefinition(beanDefinition);
        beanDefinitionMap.put(beanName, rbd);
        if (rbd.isSingleton()) {
            clearSingletonCache(beanName);
        }
        logger.debug("Registered bean definition '{}': {}", beanName, rbd.getBeanClassName());
    }

    private RootBeanDefinition toRootBeanDefinition(BeanDefinition bd) {
        if (bd instanceof RootBeanDefinition) {
            return (RootBeanDefinition) bd;
        }
        RootBeanDefinition rbd = new RootBeanDefinition();
        rbd.setBeanClassName(bd.getBeanClassName());
        rbd.setBeanClass(bd.getBeanClass());
        rbd.setScope(bd.getScope());
        rbd.setLazyInit(bd.isLazyInit());
        rbd.setDependsOn(bd.getDependsOn());
        rbd.setRole(bd.getRole());
        return rbd;
    }

    @Override
    public RootBeanDefinition getBeanDefinition(String beanName) {
        RootBeanDefinition rbd = beanDefinitionMap.get(beanName);
        if (rbd == null) {
            throw new NoSuchBeanException(beanName);
        }
        return rbd;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanDefinitionMap.keySet().toArray(new String[0]);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionMap.size();
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        beanDefinitionMap.remove(beanName);
    }

    @Override
    public Object getBean(String name) throws Exception {
        return doGetBean(name, null, null);
    }

    @Override
    public <T> T getBean(Class<T> type) throws Exception {
        return doGetBean(null, type, null);
    }

    @Override
    public <T> T getBean(String name, Class<T> type) throws Exception {
        return doGetBean(name, type, null);
    }

    @Override
    public <T> T getBean(Supplier<T> supplier) throws Exception {
        return supplier.get();
    }

    public <T> T getBean(String name, Supplier<T> supplier) throws Exception {
        return doGetBean(name, null, null);
    }

    protected <T> T doGetBean(String name, Class<T> requiredType, Object[] args) throws Exception {
        String beanName = name;
        if (name == null && requiredType != null) {
            String[] beanNames = getBeanNamesForType(requiredType);
            if (beanNames.length == 1) {
                beanName = beanNames[0];
            } else if (beanNames.length > 1) {
                throw new NoSuchBeanException(requiredType.getName(), beanNames);
            } else {
                throw new NoSuchBeanException(requiredType.getName());
            }
        }

        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null) {
            return (T) getObjectForBeanInstance(sharedInstance, beanName);
        }

        if (isSingletonCurrentlyInCreation(beanName)) {
            throw new BeanCurrentlyInCreationException(beanName);
        }

        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        if (beanDefinition == null) {
            throw new NoSuchBeanException(beanName);
        }

        if (((RootBeanDefinition) beanDefinition).isSingleton()) {
            return (T) createBean(beanName, (RootBeanDefinition) beanDefinition, args);
        } else {
            return (T) createBean(beanName, (RootBeanDefinition) beanDefinition, args);
        }
    }

    protected Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }

    protected boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonsCurrentlyInCreation.contains(beanName);
    }

    protected Object createBean(String beanName, RootBeanDefinition beanDefinition, Object[] args) throws Exception {
        logger.debug("Creating bean '{}'", beanName);

        try {
            singletonsCurrentlyInCreation.add(beanName);

            Object bean = instantiateBean(beanName, beanDefinition, args);

            bean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

            initializeBean(bean, beanName, beanDefinition);

            bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);

            if (beanDefinition.isSingleton()) {
                addSingleton(beanName, bean);
            }

            return bean;
        } finally {
            singletonsCurrentlyInCreation.remove(beanName);
        }
    }

    protected Object instantiateBean(String beanName, RootBeanDefinition beanDefinition, Object[] args) throws Exception {
        Class<?> beanClass = resolveBeanClass(beanDefinition);
        if (beanClass == null) {
            throw new IllegalStateException("Cannot determine bean class for " + beanName);
        }

        Constructor<?> constructorToUse;
        if (args != null && args.length > 0) {
            constructorToUse = beanClass.getDeclaredConstructors()[0];
        } else {
            constructorToUse = beanClass.getDeclaredConstructors()[0];
        }

        Object bean = constructorToUse.newInstance(args);
        return bean;
    }

    protected void initializeBean(Object bean, String beanName, RootBeanDefinition beanDefinition) throws Exception {
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }

        invokeInitMethods(bean, beanName);
    }

    protected void invokeInitMethods(Object bean, String beanName) throws Exception {
        // Handle custom init methods from bean definition
    }

    protected Object applyBeanPostProcessorsBeforeInitialization(Object bean, String beanName) throws Exception {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            result = processor.postProcessBeforeInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }

    protected Object applyBeanPostProcessorsAfterInitialization(Object bean, String beanName) throws Exception {
        Object result = bean;
        for (BeanPostProcessor processor : beanPostProcessors) {
            result = processor.postProcessAfterInitialization(result, beanName);
            if (result == null) {
                return result;
            }
        }
        return result;
    }

    protected Object getObjectForBeanInstance(Object beanInstance, String beanName) {
        return beanInstance;
    }

    protected void addSingleton(String beanName, Object singleton) {
        singletonObjects.put(beanName, singleton);
    }

    protected Class<?> resolveBeanClass(RootBeanDefinition beanDefinition) throws ClassNotFoundException {
        if (beanDefinition.getBeanClass() != null) {
            return beanDefinition.getBeanClass();
        }
        if (beanDefinition.getBeanClassName() != null) {
            return beanClassLoader.loadClass(beanDefinition.getBeanClassName());
        }
        return null;
    }

    @Override
    public boolean containsBean(String name) {
        return beanDefinitionMap.containsKey(name) || singletonObjects.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) throws Exception {
        RootBeanDefinition rbd = beanDefinitionMap.get(name);
        return rbd != null && rbd.isSingleton();
    }

    @Override
    public boolean isPrototype(String name) throws Exception {
        RootBeanDefinition rbd = beanDefinitionMap.get(name);
        return rbd != null && rbd.isPrototype();
    }

    @Override
    public Class<?> getType(String name) throws Exception {
        RootBeanDefinition rbd = beanDefinitionMap.get(name);
        if (rbd != null) {
            return resolveBeanClass(rbd);
        }
        Object singleton = singletonObjects.get(name);
        if (singleton != null) {
            return singleton.getClass();
        }
        return null;
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return getBeanNamesForType(type, true, true);
    }

    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type) throws Exception {
        String[] beanNames = getBeanNamesForType(type);
        Map<String, T> result = new LinkedHashMap<>();
        for (String name : beanNames) {
            T bean = getBean(name, type);
            result.put(name, bean);
        }
        return result;
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, RootBeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            RootBeanDefinition rbd = entry.getValue();
            if (rbd.isAbstract()) {
                continue;
            }
            Class<?> beanClass = null;
            try {
                beanClass = resolveBeanClass(rbd);
            } catch (Exception e) {
                continue;
            }
            if (beanClass != null && type.isAssignableFrom(beanClass)) {
                if (rbd.isSingleton() || includeNonSingletons) {
                    result.add(beanName);
                }
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, RootBeanDefinition> entry : beanDefinitionMap.entrySet()) {
            try {
                Class<?> beanClass = resolveBeanClass(entry.getValue());
                if (beanClass != null && beanClass.isAnnotationPresent(annotationType)) {
                    result.add(entry.getKey());
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return result.toArray(new String[0]);
    }

    @Override
    public Map<String, Object> getBeansWithAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) throws Exception {
        String[] beanNames = getBeanNamesForAnnotation(annotationType);
        Map<String, Object> result = new LinkedHashMap<>();
        for (String name : beanNames) {
            result.put(name, getBean(name));
        }
        return result;
    }

    public void addBeanPostProcessor(BeanPostProcessor processor) {
        beanPostProcessors.add(processor);
    }

    public List<BeanPostProcessor> getBeanPostProcessors() {
        return beanPostProcessors;
    }

    protected void clearSingletonCache(String beanName) {
        singletonObjects.remove(beanName);
    }

    public void clearSingletonCache() {
        singletonObjects.clear();
    }

    // Exception classes
    public static class NoSuchBeanException extends RuntimeException {
        private final String beanName;

        public NoSuchBeanException(String beanName) {
            super("No bean named '" + beanName + "' found");
            this.beanName = beanName;
        }

        public NoSuchBeanException(String beanName, String[] similarBeans) {
            super("No bean named '" + beanName + "' found" +
                    (similarBeans.length > 0 ? ". Similar beans: " + Arrays.toString(similarBeans) : ""));
            this.beanName = beanName;
        }

        public String getBeanName() {
            return beanName;
        }
    }

    public static class BeanCurrentlyInCreationException extends RuntimeException {
        private final String beanName;

        public BeanCurrentlyInCreationException(String beanName) {
            super("Bean named '" + beanName + "' is currently being created");
            this.beanName = beanName;
        }

        public String getBeanName() {
            return beanName;
        }
    }
}

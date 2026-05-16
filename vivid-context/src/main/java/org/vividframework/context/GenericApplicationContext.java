package org.vividframework.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanConfigurer;
import org.vividframework.beans.BeanDefinition;
import org.vividframework.beans.BeanDefinitionRegistry;
import org.vividframework.beans.BeanFactoryPostProcessor;
import org.vividframework.beans.BeanPostProcessor;
import org.vividframework.beans.DefaultListableBeanFactory;
import org.vividframework.beans.DisposableBean;
import org.vividframework.beans.RootBeanDefinition;
import org.vividframework.beans.annotation.Component;
import org.vividframework.beans.annotation.ComponentScan;
import org.vividframework.beans.config.AutowiredAnnotationBeanPostProcessor;
import org.vividframework.beans.scanner.ClassPathBeanDefinitionScanner;
import org.vividframework.config.Environment;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Generic application context implementation with full refresh lifecycle.
 * @author sketch
 */
public class GenericApplicationContext implements ApplicationContext, BeanDefinitionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(GenericApplicationContext.class);

    private final DefaultListableBeanFactory beanFactory;
    private final String id;
    private String displayName;
    private ApplicationContext parent;
    private Environment environment;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private boolean refreshed = false;

    public GenericApplicationContext() {
        this(new DefaultListableBeanFactory());
    }

    public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.id = generateId();
        this.displayName = "GenericApplicationContext";
        // Register default bean post processors
        registerDefaultBeanPostProcessors();
    }

    public GenericApplicationContext(String id) {
        this.beanFactory = new DefaultListableBeanFactory();
        this.id = id;
        this.displayName = "GenericApplicationContext";
        registerDefaultBeanPostProcessors();
    }

    private void registerDefaultBeanPostProcessors() {
        // Register AutowiredAnnotationBeanPostProcessor
        AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
        processor.setBeanFactory(beanFactory);
        beanFactory.addBeanPostProcessor(processor);
    }

    private String generateId() {
        return "generic-" + System.identityHashCode(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public ApplicationContext getParent() {
        return parent;
    }

    public void setParent(ApplicationContext parent) {
        this.parent = parent;
    }

    @Override
    public Environment getEnvironment() {
        if (environment == null) {
            environment = new Environment.StandardEnvironment();
        }
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void start() {
        active.set(true);
    }

    @Override
    public void stop() {
        active.set(false);
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public boolean isRunning() {
        return active.get() && !closed.get();
    }

    @Override
    public void close() {
        if (closed.get()) {
            return;
        }
        closed.set(true);
        active.set(false);
        refreshed = false;
        
        // Destroy singleton beans
        destroySingletons();
    }

    protected void destroySingletons() {
        logger.debug("Destroying singletons in context '{}'", this);
        Map<String, Object> singletons = beanFactory.getSingletonObjects();
        for (Map.Entry<String, Object> entry : singletons.entrySet()) {
            Object bean = entry.getValue();
            if (bean instanceof DisposableBean) {
                try {
                    ((DisposableBean) bean).destroy();
                    logger.debug("Destroyed singleton '{}'", entry.getKey());
                } catch (Exception e) {
                    logger.warn("Error destroying singleton '{}'", entry.getKey(), e);
                }
            }
        }
        beanFactory.clearSingletonCache();
    }

    @Override
    public Object getBean(String name) throws Exception {
        return beanFactory.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> type) throws Exception {
        return beanFactory.getBean(type);
    }

    @Override
    public <T> T getBean(String name, Class<T> type) throws Exception {
        return beanFactory.getBean(name, type);
    }

    @Override
    public <T> T getBean(Supplier<T> supplier) throws Exception {
        return beanFactory.getBean(supplier);
    }

    @Override
    public boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    @Override
    public boolean isSingleton(String name) throws Exception {
        return beanFactory.isSingleton(name);
    }

    @Override
    public boolean isPrototype(String name) throws Exception {
        return beanFactory.isPrototype(name);
    }

    @Override
    public Class<?> getType(String name) throws Exception {
        return beanFactory.getType(name);
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return beanFactory.getBeanDefinitionNames();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return beanFactory.getBeanNamesForType(type);
    }

    @Override
    public <T> java.util.Map<String, T> getBeansOfType(Class<T> type) throws Exception {
        return beanFactory.getBeansOfType(type);
    }

    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) {
        return beanFactory.getBeanNamesForAnnotation(annotationType);
    }

    @Override
    public java.util.Map<String, Object> getBeansWithAnnotation(Class<? extends java.lang.annotation.Annotation> annotationType) throws Exception {
        return beanFactory.getBeansWithAnnotation(annotationType);
    }

    @Override
    public boolean containsBeanDefinition(String name) {
        return beanFactory.containsBeanDefinition(name);
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanFactory.getBeanDefinitionCount();
    }

    @Override
    public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
        return beanFactory.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void removeBeanDefinition(String beanName) {
        beanFactory.removeBeanDefinition(beanName);
    }

    @Override
    public RootBeanDefinition getBeanDefinition(String beanName) {
        return beanFactory.getBeanDefinition(beanName);
    }

    public DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return beanFactory;
    }

    /**
     * Register a bean definition from component scanning
     */
    public void scan(String... basePackages) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this);
        scanner.scan(basePackages);
    }

    /**
     * Register configuration class and scan its packages
     */
    public void register(Class<?>... configurationClasses) {
        for (Class<?> configClass : configurationClasses) {
            if (configClass.isAnnotationPresent(Component.class)) {
                registerBeanDefinition(ClassPathBeanDefinitionScanner.getBeanName(configClass), 
                        new RootBeanDefinition(configClass));
            }
            
            // Scan from ComponentScan
            if (configClass.isAnnotationPresent(ComponentScan.class)) {
                ComponentScan componentScan = configClass.getAnnotation(ComponentScan.class);
                ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this);
                scanner.scanFromComponentScan(configClass);
            }
        }
    }

    /**
     * Refresh the application context.
     * This method performs the complete initialization sequence.
     */
    public void refresh() throws Exception {
        if (refreshed) {
            throw new IllegalStateException("GenericApplicationContext has already been refreshed");
        }
        if (closed.get()) {
            throw new IllegalStateException("GenericApplicationContext has been closed");
        }

        try {
            // Step 1: Prepare refresh
            prepareRefresh();

            // Step 2: Prepare the bean factory
            prepareBeanFactory(beanFactory);

            // Step 3: Post-process the bean factory (subclasses can override)
            postProcessBeanFactory(beanFactory);

            // Step 4: Invoke factory processors
            invokeBeanFactoryProcessors(beanFactory);

            // Step 4.5: Process BeanConfigurer registrations
            invokeBeanConfigurers(beanFactory);

            // Step 5: Register bean post-processors
            registerBeanPostProcessors(beanFactory);

            // Step 6: Initialize the bean factory
            initMessageSource(beanFactory);
            initApplicationEventMulticaster(beanFactory);
            onRefresh(beanFactory);

            // Step 7: Check for listeners and publish events
            registerListeners();

            // Step 8: Instantiate singletons
            finishBeanFactoryInitialization(beanFactory);

            // Step 9: Finish refresh
            finishRefresh();

            refreshed = true;
        } catch (Exception e) {
            logger.error("Failed to refresh context", e);
            destroyBeans();
            throw e;
        }
    }

    protected void prepareRefresh() {
        active.set(true);
        closed.set(false);
        logger.info("Starting refresh of GenericApplicationContext '{}'", this.id);
    }

    protected void prepareBeanFactory(DefaultListableBeanFactory beanFactory) {
        // Configure bean factory settings
        beanFactory.setAllowCircularReferences(true);
        logger.debug("Prepared bean factory with {} bean definitions", beanFactory.getBeanDefinitionCount());
    }

    protected void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) {
        // Subclasses can override to add post-processing
    }

    protected void invokeBeanFactoryProcessors(DefaultListableBeanFactory beanFactory) {
        List<BeanFactoryPostProcessor> processors = new ArrayList<>();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            try {
                RootBeanDefinition bd = beanFactory.getBeanDefinition(beanName);
                Class<?> beanClass = resolveBeanClass(bd);
                if (beanClass != null && BeanFactoryPostProcessor.class.isAssignableFrom(beanClass)) {
                    BeanFactoryPostProcessor processor = (BeanFactoryPostProcessor) beanClass
                            .getDeclaredConstructor().newInstance();
                    processors.add(processor);
                }
            } catch (Exception e) {
                logger.debug("Skipping BeanFactoryPostProcessor '{}': {}", beanName, e.getMessage());
            }
        }

        for (BeanFactoryPostProcessor processor : processors) {
            try {
                processor.postProcessBeanFactory(beanFactory);
            } catch (Exception e) {
                logger.error("BeanFactoryPostProcessor {} failed", processor.getClass().getName(), e);
            }
        }

        logger.debug("Invoked {} bean factory processors", processors.size());
    }

    protected void invokeBeanConfigurers(DefaultListableBeanFactory beanFactory) {
        int count = 0;
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            try {
                RootBeanDefinition bd = beanFactory.getBeanDefinition(beanName);
                Class<?> beanClass = resolveBeanClass(bd);
                if (beanClass != null && BeanConfigurer.class.isAssignableFrom(beanClass)) {
                    BeanConfigurer configurer = (BeanConfigurer) beanClass.getDeclaredConstructor().newInstance();
                    configurer.configure(beanFactory);
                    count++;
                }
            } catch (Exception e) {
                logger.debug("Skipping BeanConfigurer '{}': {}", beanName, e.getMessage());
            }
        }
        logger.debug("Invoked {} bean configurers", count);
    }

    private Class<?> resolveBeanClass(RootBeanDefinition bd) {
        try {
            if (bd.getBeanClass() != null) {
                return bd.getBeanClass();
            }
            if (bd.getBeanClassName() != null) {
                return Thread.currentThread().getContextClassLoader().loadClass(bd.getBeanClassName());
            }
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    protected void registerBeanPostProcessors(DefaultListableBeanFactory beanFactory) {
        // Ensure AutowiredAnnotationBeanPostProcessor is registered first
        boolean hasAutowiredProcessor = false;
        for (BeanPostProcessor bpp : beanFactory.getBeanPostProcessors()) {
            if (bpp instanceof AutowiredAnnotationBeanPostProcessor) {
                hasAutowiredProcessor = true;
                break;
            }
        }
        
        if (!hasAutowiredProcessor) {
            AutowiredAnnotationBeanPostProcessor processor = new AutowiredAnnotationBeanPostProcessor();
            processor.setBeanFactory(beanFactory);
            beanFactory.addBeanPostProcessor(processor);
        }
        
        logger.debug("Registered {} bean post processors", beanFactory.getBeanPostProcessors().size());
    }

    protected void initMessageSource(DefaultListableBeanFactory beanFactory) {
        // No-op: would need MessageSource interface
    }

    protected void initApplicationEventMulticaster(DefaultListableBeanFactory beanFactory) {
        // No-op: would need ApplicationEventMulticaster
    }

    protected void onRefresh(DefaultListableBeanFactory beanFactory) throws Exception {
        // Subclasses can override to create internal beans
    }

    protected void registerListeners() {
        // No-op: would need ApplicationEvent infrastructure
    }

    protected void finishBeanFactoryInitialization(DefaultListableBeanFactory beanFactory) throws Exception {
        // Instantiate all singleton beans
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            RootBeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            if (!bd.isAbstract() && bd.isSingleton() && !bd.hasPropertyValues()) {
                try {
                    beanFactory.getBean(beanName);
                } catch (Exception e) {
                    logger.debug("Failed to pre-instantiate bean '{}': {}", beanName, e.getMessage());
                }
            }
        }
        logger.info("Finished bean factory initialization with {} singletons", beanFactory.getBeanDefinitionCount());
    }

    protected void finishRefresh() {
        start();
        logger.info("Completed refresh of GenericApplicationContext '{}'", this.id);
    }

    protected void destroyBeans() {
        beanFactory.clearSingletonCache();
        refreshed = false;
    }
}

package org.vividframework.context;

import org.vividframework.beans.BeanDefinition;
import org.vividframework.beans.BeanDefinitionRegistry;
import org.vividframework.beans.DefaultListableBeanFactory;
import org.vividframework.config.Environment;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Generic application context implementation
 * @author Jon Fisher
 */
public class GenericApplicationContext implements ApplicationContext, BeanDefinitionRegistry {

    private final DefaultListableBeanFactory beanFactory;
    private final String id;
    private final String displayName;
    private ApplicationContext parent;
    private Environment environment;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public GenericApplicationContext() {
        this(new DefaultListableBeanFactory());
    }

    public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        this.id = generateId();
        this.displayName = "GenericApplicationContext";
    }

    public GenericApplicationContext(String id) {
        this.beanFactory = new DefaultListableBeanFactory();
        this.id = id;
        this.displayName = "GenericApplicationContext";
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
        this.beanFactory.getClass(); // Ensure beanFactory exists
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
        closed.set(true);
        active.set(false);
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
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanFactory.getBeanDefinition(beanName);
    }

    public DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return beanFactory;
    }

    public void refresh() throws Exception {
        prepareRefresh();
        // Configure bean factory
        prepareBeanFactory(beanFactory);
        // Post-process bean factory
        postProcessBeanFactory(beanFactory);
        // Invoke factory processors
        invokeBeanFactoryProcessors(beanFactory);
        // Register bean post-processors
        registerBeanPostProcessors(beanFactory);
        // Initialize bean factory
        finishBeanFactoryInitialization(beanFactory);
        // Finish refresh
        finishRefresh();
    }

    protected void prepareRefresh() {
        active.set(true);
    }

    protected void prepareBeanFactory(DefaultListableBeanFactory beanFactory) {
        // Configure bean factory
    }

    protected void postProcessBeanFactory(DefaultListableBeanFactory beanFactory) {
        // Post-process
    }

    protected void invokeBeanFactoryProcessors(DefaultListableBeanFactory beanFactory) {
        // Invoke processors
    }

    protected void registerBeanPostProcessors(DefaultListableBeanFactory beanFactory) {
        // Register post processors
    }

    protected void finishBeanFactoryInitialization(DefaultListableBeanFactory beanFactory) throws Exception {
        // Initialize singleton beans
    }

    protected void finishRefresh() {
        start();
    }
}

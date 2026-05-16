package org.vividframework.aop.autoproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.aop.Advised;
import org.vividframework.aop.Advisor;
import org.vividframework.aop.CglibAopProxy;
import org.vividframework.aop.Pointcut;
import org.vividframework.beans.BeanFactory;
import org.vividframework.beans.BeanPostProcessor;

import java.lang.reflect.Method;

/**
 * Auto-proxy creator that wraps beans with AOP proxies based on advisor/pointcut configuration.
 * This is a simplified implementation - full AspectJ annotation support would require
 * @Aspect, @Before, @After, @Around annotations processing.
 * @author sketch
 */
public class AnnotationAwareAspectJAutoProxyCreator implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationAwareAspectJAutoProxyCreator.class);

    private BeanFactory beanFactory;
    private boolean proxyTargetClass = true;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setProxyTargetClass(boolean proxyTargetClass) {
        this.proxyTargetClass = proxyTargetClass;
    }

    public boolean isProxyTargetClass() {
        return proxyTargetClass;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Exception {
        if (bean == null) {
            return null;
        }

        // Check if this bean should be wrapped with a proxy
        if (!isInfrastructureClass(bean.getClass())) {
            // Look for advisors that match this bean
            Class<?> targetClass = bean.getClass();
            Advised.AdvisedSupport advisedSupport = createAdvisedSupport(targetClass);
            
            if (advisedSupport.hasAdvisors()) {
                return wrapWithProxy(bean, targetClass, advisedSupport);
            }
        }

        return bean;
    }

    protected boolean isInfrastructureClass(Class<?> clazz) {
        // Don't proxy infrastructure classes
        return clazz.getName().contains("$$") || // Already a proxy
               clazz.getName().startsWith("org.vividframework.beans.config.") ||
               clazz.getName().startsWith("org.vividframework.aop.autoproxy");
    }

    protected Advised.AdvisedSupport createAdvisedSupport(Class<?> targetClass) {
        Advised.AdvisedSupport advisedSupport = new Advised.AdvisedSupport(targetClass);
        advisedSupport.setProxyTargetClass(proxyTargetClass);
        
        // In a full implementation, this would look up advisors from the bean factory
        // based on pointcut matching
        
        return advisedSupport;
    }

    protected Object wrapWithProxy(Object target, Class<?> targetClass, Advised.AdvisedSupport advisedSupport) {
        advisedSupport.setTarget(target);
        
        try {
            Object proxy;
            if (proxyTargetClass || targetClass.isInterface()) {
                proxy = advisedSupport.getProxy();
            } else {
                proxy = new CglibAopProxy(advisedSupport).getProxy(targetClass.getClassLoader());
            }
            
            logger.debug("Created {} proxy for bean of type: {}", 
                    proxyTargetClass ? "CGLIB" : "JDK", 
                    targetClass.getName());
            
            return proxy;
        } catch (Exception e) {
            logger.error("Failed to create proxy for bean: {}", targetClass.getName(), e);
            return target;
        }
    }

    /**
     * Add an advisor to be applied to all proxy creations
     */
    public void addAdvisor(Advisor advisor) {
        // Store for later use during proxy creation
        logger.debug("Registered advisor: {}", advisor.getClass().getName());
    }

    /**
     * Check if a specific method matches the pointcuts
     */
    protected boolean matches(Method method, Class<?> targetClass) {
        // Simplified: would need pointcut evaluation
        return true;
    }
}

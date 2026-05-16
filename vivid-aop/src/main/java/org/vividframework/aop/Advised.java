package org.vividframework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * AOP proxy support
 * @author Jon Fisher
 */
public interface Advised {

    /**
     * Get target class
     */
    Class<?> getTargetClass();

    /**
     * Get target object
     */
    Object getTarget();

    /**
     * Get advisors
     */
    List<Advisor> getAdvisors();

    /**
     * Add advisor
     */
    void addAdvisor(Advisor advisor);

    /**
     * Remove advisor
     */
    boolean removeAdvisor(Advisor advisor);

    /**
     * Check if has advisors
     */
    boolean hasAdvisors();

    /**
     * Get proxy
     */
    Object getProxy();

    /**
     * Get proxy with class loader
     */
    Object getProxy(ClassLoader classLoader);

    /**
     * Default implementation
     */
    class AdvisedSupport implements Advised {

        private final Class<?> targetClass;
        private Object target;
        private final List<Advisor> advisors = new ArrayList<>();
        private boolean proxyTargetClass = false;

        public AdvisedSupport(Object target) {
            this.target = target;
            this.targetClass = target.getClass();
        }

        public AdvisedSupport(Class<?> targetClass) {
            this.target = null;
            this.targetClass = targetClass;
            this.proxyTargetClass = true;
        }

        @Override
        public Class<?> getTargetClass() {
            return targetClass;
        }

        @Override
        public Object getTarget() {
            return target;
        }

        @Override
        public List<Advisor> getAdvisors() {
            return advisors;
        }

        @Override
        public void addAdvisor(Advisor advisor) {
            advisors.add(advisor);
        }

        @Override
        public boolean removeAdvisor(Advisor advisor) {
            return advisors.remove(advisor);
        }

        @Override
        public boolean hasAdvisors() {
            return !advisors.isEmpty();
        }

        @Override
        public Object getProxy() {
            return getProxy(getClass().getClassLoader());
        }

        @Override
        public Object getProxy(ClassLoader classLoader) {
            if (proxyTargetClass || targetClass.isInterface()) {
                return Proxy.newProxyInstance(classLoader, new Class[]{targetClass}, new JdkDynamicAopProxy(this));
            }
            return new CglibAopProxy(this).getProxy(classLoader);
        }

        public void setTarget(Object target) {
            this.target = target;
        }

        public boolean isProxyTargetClass() {
            return proxyTargetClass;
        }

        public void setProxyTargetClass(boolean proxyTargetClass) {
            this.proxyTargetClass = proxyTargetClass;
        }
    }

    /**
     * JDK dynamic proxy implementation
     */
    class JdkDynamicAopProxy implements InvocationHandler {

        private final AdvisedSupport advised;

        public JdkDynamicAopProxy(AdvisedSupport advised) {
            this.advised = advised;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MethodInvocation invocation = new MethodInvocation() {
                private int currentInterceptorIndex = -1;
                private final List<Advisor> advisors = new ArrayList<>(advised.getAdvisors());

                @Override
                public Object proceed() throws Throwable {
                    if (++currentInterceptorIndex >= advisors.size()) {
                        return method.invoke(advised.getTarget(), args);
                    }
                    Advisor advisor = advisors.get(currentInterceptorIndex);
                    if (advisor.getAdvice() instanceof MethodInterceptor) {
                        return ((MethodInterceptor) advisor.getAdvice()).invoke((org.vividframework.aop.MethodInvocation) this);
                    }
                    return proceed();
                }

                @Override
                public Method getMethod() {
                    return method;
                }

                @Override
                public Object[] getArguments() {
                    return args;
                }

                @Override
                public Object getTarget() {
                    return advised.getTarget();
                }

                @Override
                public Class<?> getDeclaringType() {
                    return method.getDeclaringClass();
                }
            };

            try {
                return invocation.proceed();
            } catch (Exception e) {
                throw e;
            }
        }
    }

    /**
     * CGLIB proxy implementation - moved to CglibAopProxy.java
     * This class is kept for backward compatibility but delegates to the actual implementation
     */
    @Deprecated
    class CglibAopProxy {

        private final AdvisedSupport advised;

        public CglibAopProxy(AdvisedSupport advised) {
            this.advised = advised;
        }

        public Object getProxy(ClassLoader classLoader) {
            // Delegate to the actual CglibAopProxy implementation
            return new org.vividframework.aop.CglibAopProxy(advised).getProxy(classLoader);
        }
    }

    /**
     * Method invocation interface
     */
    interface MethodInvocation extends org.vividframework.aop.MethodInvocation {

        Class<?> getDeclaringType();
    }
}

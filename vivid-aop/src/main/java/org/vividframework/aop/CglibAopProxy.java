package org.vividframework.aop;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * CGLIB-based AOP proxy implementation.
 * Creates subclass proxies for classes that don't implement interfaces.
 * @author sketch
 */
public class CglibAopProxy {

    private static final Logger logger = LoggerFactory.getLogger(CglibAopProxy.class);

    private final Advised advised;

    public CglibAopProxy(Advised advised) {
        this.advised = advised;
    }

    /**
     * Get proxy using CGLIB
     */
    public Object getProxy(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }

        Class<?> targetClass = advised.getTargetClass();
        if (targetClass == null) {
            throw new IllegalStateException("Target class must be set for CGLIB proxy");
        }

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setClassLoader(classLoader);
        
        // Set callbacks
        Callback[] callbacks = createCallbacks();
        enhancer.setCallbacks(callbacks);
        
        // Exclude equals, hashCode, toString from proxy
        enhancer.setInterceptDuringConstruction(false);

        Object proxy = enhancer.create();
        logger.debug("Created CGLIB proxy for class: {}", targetClass.getName());
        return proxy;
    }

    private Callback[] createCallbacks() {
        return new Callback[] {
            new MethodInterceptor() {
                @Override
                public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                    // Skip Object methods that shouldn't be intercepted
                    if (method.getDeclaringClass() == Object.class) {
                        if ("equals".equals(method.getName())) {
                            return proxy == args[0];
                        } else if ("hashCode".equals(method.getName())) {
                            return System.identityHashCode(proxy);
                        } else if ("toString".equals(method.getName())) {
                            return "CGLIB proxy for " + advised.getTargetClass().getName();
                        }
                    }

                    // Create method invocation
                    Advised.MethodInvocation invocation = new Advised.MethodInvocation() {
                        private int currentInterceptorIndex = -1;
                        private final List<Advisor> advisors = advised.getAdvisors();

                        @Override
                        public Object proceed() throws Throwable {
                            if (++currentInterceptorIndex >= advisors.size()) {
                                // Execute target method
                                Object target = advised.getTarget();
                                if (target == null) {
                                    throw new IllegalStateException("Target is null for proxy method: " + method.getName());
                                }
                                return methodProxy.invokeSuper(proxy, args);
                            }
                            Advisor advisor = advisors.get(currentInterceptorIndex);
                            if (advisor.getAdvice() instanceof org.vividframework.aop.MethodInterceptor) {
                                return ((org.vividframework.aop.MethodInterceptor) advisor.getAdvice()).invoke(this);
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
                            return proxy;
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

                private Object getProxy() {
                    return CglibAopProxy.this;
                }
            }
        };
    }
}

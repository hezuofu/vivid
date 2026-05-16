package org.vividframework.aop;

import java.lang.reflect.Method;

/**
 * Advisor interface
 * @author sketch
 */
public interface Advisor {

    /**
     * Get advice
     */
    Advice getAdvice();

    /**
     * Check if per-instance
     */
    default boolean isPerInstance() {
        return false;
    }

    /**
     * Simple advisor implementation
     */
    class StaticMethodMatcherPointcutAdvisor implements Advisor {

        private final Advice advice;
        private final org.vividframework.aop.support.StaticMethodMatcherPointcut pointcut;

        public StaticMethodMatcherPointcutAdvisor(Advice advice) {
            this.advice = advice;
            this.pointcut = new org.vividframework.aop.support.StaticMethodMatcherPointcut() {
                @Override
                public boolean matches(Method method, Class<?> targetClass) {
                    return true;
                }
            };
        }

        public StaticMethodMatcherPointcutAdvisor(Advice advice, org.vividframework.aop.support.StaticMethodMatcherPointcut pointcut) {
            this.advice = advice;
            this.pointcut = pointcut;
        }

        @Override
        public Advice getAdvice() {
            return advice;
        }

        public org.vividframework.aop.support.StaticMethodMatcherPointcut getPointcut() {
            return pointcut;
        }
    }
}

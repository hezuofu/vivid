package org.vividframework.aop;

import java.lang.reflect.Method;

/**
 * Pointcut interface
 * @author Jon Fisher
 */
public interface Pointcut {

    /**
     * Get class filter
     */
    ClassFilter getClassFilter();

    /**
     * Get method matcher
     */
    MethodMatcher getMethodMatcher();

    /**
     * Check if matches
     */
    boolean matches(Method method, Class<?> targetClass);

    /**
     * Always true pointcut
     */
    Pointcut TRUE = new Pointcut() {
        @Override
        public ClassFilter getClassFilter() {
            return ClassFilter.TRUE;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return MethodMatcher.TRUE;
        }

        @Override
        public boolean matches(Method method, Class<?> targetClass) {
            return getMethodMatcher().matches(method, targetClass) && getClassFilter().matches(targetClass);
        }
    };

    /**
     * Class filter
     */
    interface ClassFilter {
        boolean matches(Class<?> clazz);

        ClassFilter TRUE = clazz -> true;
    }

    /**
     * Method matcher
     */
    interface MethodMatcher {
        boolean matches(Method method, Class<?> targetClass);

        boolean isRuntime();

        boolean matches(Method method, Class<?> targetClass, Object... args);

        MethodMatcher TRUE = new MethodMatcher() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return true;
            }

            @Override
            public boolean isRuntime() {
                return false;
            }

            @Override
            public boolean matches(Method method, Class<?> targetClass, Object... args) {
                return false;
            }
        };
    }
}

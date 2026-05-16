package org.vividframework.aop.support;

import org.vividframework.aop.Pointcut;

import java.lang.reflect.Method;

/**
 * Static method matcher pointcut
 * @author sketch
 */
public abstract class StaticMethodMatcherPointcut implements Pointcut.MethodMatcher {

    @Override
    public boolean isRuntime() {
        return false;
    }

    @Override
    public final boolean matches(Method method, Class<?> targetClass, Object... args) {
        return matches(method, targetClass);
    }
}

package org.vividframework.aop.support;

import org.vividframework.aop.Advice;
import org.vividframework.aop.Advisor;
import org.vividframework.aop.Pointcut;
import org.vividframework.aop.MethodMatcher;

import java.lang.reflect.Method;

/**
 * Static method matcher pointcut advisor.
 * Used for advisors that apply to specific methods based on static matching.
 * @author Jon Fisher
 */
public class StaticMethodMatcherPointcutAdvisor implements Advisor {

    private Advice advice;
    private Pointcut pointcut = Pointcut.TRUE;

    public StaticMethodMatcherPointcutAdvisor() {
    }

    public StaticMethodMatcherPointcutAdvisor(Advice advice) {
        this.advice = advice;
    }

    public StaticMethodMatcherPointcutAdvisor(Advice advice, Pointcut pointcut) {
        this.advice = advice;
        this.pointcut = pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    public void setAdvice(Advice advice) {
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    public void setPointcut(Pointcut pointcut) {
        this.pointcut = pointcut;
    }

    /**
     * Check if this advisor matches the given method
     */
    public boolean matches(Method method, Class<?> targetClass) {
        Pointcut p = getPointcut();
        if (p != null) {
            if (!p.getClassFilter().matches(targetClass)) {
                return false;
            }
            MethodMatcher mm = p.getMethodMatcher();
            if (mm != null) {
                return mm.matches(method, targetClass);
            }
        }
        return true;
    }

    /**
     * Create an advisor for a specific advice and method matcher
     */
    public static StaticMethodMatcherPointcutAdvisor create(Advice advice, MethodMatcher methodMatcher) {
        return new StaticMethodMatcherPointcutAdvisor(advice, 
            new Pointcut() {
                @Override
                public ClassFilter getClassFilter() {
                    return ClassFilter.TRUE;
                }

                @Override
                public MethodMatcher getMethodMatcher() {
                    return methodMatcher;
                }
            });
    }
}

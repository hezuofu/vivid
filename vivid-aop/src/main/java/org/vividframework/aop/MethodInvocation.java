package org.vividframework.aop;

import java.lang.reflect.Method;

/**
 * Method invocation interface for AOP
 * @author Jon Fisher
 */
public interface MethodInvocation {

    /**
     * Get the method being invoked
     */
    Method getMethod();

    /**
     * Get the target object
     */
    Object getTarget();

    /**
     * Get the arguments
     */
    Object[] getArguments();

    /**
     * Proceed with the invocation
     */
    Object proceed() throws Throwable;
}

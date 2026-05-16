package org.vividframework.aop;

/**
 * Method interceptor interface
 * @author sketch
 */
public interface MethodInterceptor extends Advice {

    /**
     * Invoke the method
     */
    Object invoke(MethodInvocation invocation) throws Throwable;
}

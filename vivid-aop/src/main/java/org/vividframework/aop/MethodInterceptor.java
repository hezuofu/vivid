package org.vividframework.aop;

/**
 * Method interceptor interface
 * @author Jon Fisher
 */
public interface MethodInterceptor extends Advice {

    /**
     * Invoke the method
     */
    Object invoke(MethodInvocation invocation) throws Throwable;
}

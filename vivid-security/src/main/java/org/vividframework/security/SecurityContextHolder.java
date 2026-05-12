package org.vividframework.security;

/**
 * Security context holder
 * @author Jon Fisher
 */
public class SecurityContextHolder {

    private static final ThreadLocal<SecurityContext> contextHolder = ThreadLocal.withInitial(SecurityContextImpl::new);

    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }

    public static SecurityContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

    public static SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }
}

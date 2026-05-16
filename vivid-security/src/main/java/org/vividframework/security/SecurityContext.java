package org.vividframework.security;

/**
 * Security context interface
 * @author sketch
 */
public interface SecurityContext {

    Authentication getAuthentication();

    void setAuthentication(Authentication authentication);

    boolean isAuthenticated();
}

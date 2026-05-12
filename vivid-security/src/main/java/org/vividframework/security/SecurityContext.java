package org.vividframework.security;

/**
 * Security context interface
 * @author Jon Fisher
 */
public interface SecurityContext {

    Authentication getAuthentication();

    void setAuthentication(Authentication authentication);

    boolean isAuthenticated();
}

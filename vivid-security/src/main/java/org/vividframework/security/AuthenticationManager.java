package org.vividframework.security;

/**
 * Interface for authentication management
 * @author sketch
 */
public interface AuthenticationManager {

    /**
     * Authenticate the provided token
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException;

    /**
     * Parent authentication manager for delegation
     */
    AuthenticationManager getParent();

    /**
     * Set parent authentication manager
     */
    void setParent(AuthenticationManager parent);
}

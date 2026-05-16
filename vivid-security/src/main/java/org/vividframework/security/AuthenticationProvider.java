package org.vividframework.security;

/**
 * Interface for authentication providers
 * @author sketch
 */
public interface AuthenticationProvider {

    /**
     * Authenticate the token
     */
    Authentication authenticate(Authentication authentication) throws AuthenticationException;

    /**
     * Check if this provider supports the given authentication type
     */
    boolean supports(Class<?> authentication);
}

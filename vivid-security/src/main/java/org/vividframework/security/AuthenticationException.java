package org.vividframework.security;

/**
 * Base class for authentication exceptions
 * @author sketch
 */
public class AuthenticationException extends RuntimeException {

    private final Authentication authentication;

    public AuthenticationException(String message) {
        super(message);
        this.authentication = null;
    }

    public AuthenticationException(String message, Authentication authentication) {
        super(message);
        this.authentication = authentication;
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.authentication = null;
    }

    public AuthenticationException(String message, Authentication authentication, Throwable cause) {
        super(message, cause);
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}

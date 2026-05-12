package org.vividframework.security;

/**
 * Exception thrown for bad credentials
 * @author Jon Fisher
 */
public class BadCredentialsException extends AuthenticationException {

    public BadCredentialsException(String message) {
        super(message);
    }

    public BadCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}

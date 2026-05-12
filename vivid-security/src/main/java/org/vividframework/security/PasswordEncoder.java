package org.vividframework.security;

/**
 * Interface for password encoding
 * @author Jon Fisher
 */
public interface PasswordEncoder {

    /**
     * Encode the raw password
     */
    String encode(CharSequence rawPassword);

    /**
     * Check if the encoded password matches the raw password
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);

    /**
     * Check if the encoded password needs to be re-encoded
     */
    default boolean upgradeEncoding(String encodedPassword) {
        return false;
    }
}

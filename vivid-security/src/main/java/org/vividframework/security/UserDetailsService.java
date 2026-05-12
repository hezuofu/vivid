package org.vividframework.security;

import java.util.Collection;

/**
 * Interface for loading user-specific data
 * @author Jon Fisher
 */
public interface UserDetailsService {

    /**
     * Load user by username
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * Exception thrown when user is not found
     */
    class UsernameNotFoundException extends RuntimeException {
        private final String username;

        public UsernameNotFoundException(String username) {
            super("User not found: " + username);
            this.username = username;
        }

        public UsernameNotFoundException(String username, Throwable cause) {
            super("User not found: " + username, cause);
            this.username = username;
        }

        public String getUsername() {
            return username;
        }
    }
}

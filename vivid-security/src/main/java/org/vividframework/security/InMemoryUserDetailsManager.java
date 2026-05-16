package org.vividframework.security;

import java.util.*;

/**
 * In-memory implementation of UserDetailsService
 * @author sketch
 */
public class InMemoryUserDetailsManager implements UserDetailsService {

    private final Map<String, UserDetails> users = new HashMap<>();

    public InMemoryUserDetailsManager() {
    }

    public InMemoryUserDetailsManager(Collection<UserDetails> users) {
        for (UserDetails user : users) {
            createUser(user);
        }
    }

    public InMemoryUserDetailsManager(UserDetails... users) {
        for (UserDetails user : users) {
            createUser(user);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDetails user = users.get(username.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return user;
    }

    public void createUser(UserDetails user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    public void deleteUser(String username) {
        users.remove(username.toLowerCase());
    }

    public void updateUser(UserDetails user) {
        users.put(user.getUsername().toLowerCase(), user);
    }

    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    public void changePassword(String oldPassword, String newPassword) {
        // In production, verify old password
    }

    public Collection<UserDetails> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}

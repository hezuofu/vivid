package org.vividframework.security;

import java.util.Collection;

/**
 * Authentication interface
 * @author sketch
 */
public interface Authentication {

    Collection<? extends GrantedAuthority> getAuthorities();

    Object getCredentials();

    Object getDetails();

    Object getPrincipal();

    boolean isAuthenticated();

    void setAuthenticated(boolean isAuthenticated);

    String getName();
}

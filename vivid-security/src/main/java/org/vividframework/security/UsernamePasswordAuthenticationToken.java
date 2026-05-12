package org.vividframework.security;

import java.util.Collection;

/**
 * Username password authentication token
 * @author Jon Fisher
 */
public class UsernamePasswordAuthenticationToken implements Authentication {

    private Object principal;
    private Object credentials;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean authenticated = false;

    public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        this.principal = principal;
        this.credentials = credentials;
    }

    public UsernamePasswordAuthenticationToken(Object principal, Object credentials,
                                                Collection<? extends GrantedAuthority> authorities) {
        this.principal = principal;
        this.credentials = credentials;
        this.authorities = authorities;
        this.authenticated = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        if (principal instanceof String) {
            return (String) principal;
        }
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
}

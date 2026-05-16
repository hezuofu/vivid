package org.vividframework.security;

import java.util.Collection;

/**
 * User details interface
 * @author sketch
 */
public interface UserDetails extends Authentication {

    String getUsername();

    String getPassword();

    boolean isAccountNonExpired();

    boolean isAccountNonLocked();

    boolean isCredentialsNonExpired();

    boolean isEnabled();

    static UserDetails create(String username, String password, String... roles) {
        return new UserDetails() {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                GrantedAuthority[] authorities = new GrantedAuthority[roles.length];
                for (int i = 0; i < roles.length; i++) {
                    authorities[i] = new SimpleGrantedAuthority(roles[i].startsWith("ROLE_") ? roles[i] : "ROLE_" + roles[i]);
                }
                return java.util.Arrays.asList(authorities);
            }

            @Override
            public Object getPrincipal() {
                return username;
            }

            @Override
            public Object getCredentials() {
                return password;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {
            }

            @Override
            public String getName() {
                return username;
            }
        };
    }
}

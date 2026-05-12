package org.vividframework.security;

/**
 * Security context implementation
 * @author Jon Fisher
 */
public class SecurityContextImpl implements SecurityContext {

    private Authentication authentication;

    @Override
    public Authentication getAuthentication() {
        return authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public boolean isAuthenticated() {
        return authentication != null && authentication.isAuthenticated();
    }
}

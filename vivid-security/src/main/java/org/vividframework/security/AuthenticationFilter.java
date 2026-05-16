package org.vividframework.security;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;

/**
 * Security filter for authentication
 * @author Jon Fisher
 */
public class AuthenticationFilter implements org.vividframework.web.filter.Filter {

    private AuthenticationManager authenticationManager;
    private boolean ignoreFailure = false;

    public AuthenticationFilter() {
    }

    public AuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public HttpServletResponse doFilter(HttpServerRequest request,
                                        org.vividframework.web.filter.FilterChain chain) throws Exception {
        // Check if authentication is required
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        
        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            if (ignoreFailure) {
                return chain.doFilter(request);
            }
            
            try {
                Authentication result = attemptAuthentication(request);
                if (result != null) {
                    SecurityContextHolder.getContext().setAuthentication(result);
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                return HttpServletResponse.builder()
                        .status(401)
                        .content("Authentication failed: " + e.getMessage())
                        .build();
            }
        }
        
        return chain.doFilter(request);
    }

    protected Authentication attemptAuthentication(HttpServerRequest request) throws Exception {
        // Override in subclasses
        return null;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setIgnoreFailure(boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }
}

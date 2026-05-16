package org.vividframework.web.filter;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpMethod;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;
import org.vividframework.http.HttpServerRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CORS (Cross-Origin Resource Sharing) filter
 * @author sketch
 */
public class CorsFilter extends OncePerRequestFilter {

    private String allowedOrigin = "*";
    private Set<String> allowedOrigins = new HashSet<>();
    private boolean allowCredentials = true;
    private long maxAge = 3600;
    private Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(
            HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE,
            HttpMethod.OPTIONS, HttpMethod.PATCH, HttpMethod.HEAD, HttpMethod.TRACE
    ));
    private Set<String> allowedHeaders = new HashSet<>(Arrays.asList(
            HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT,
            HttpHeaders.AUTHORIZATION, "X-Requested-With"
    ));
    private Set<String> exposedHeaders = new HashSet<>();

    public CorsFilter() {
    }

    public void setAllowedOrigin(String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    public void setAllowedOrigins(Set<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public void setAllowedMethods(Set<HttpMethod> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public void setAllowedHeaders(Set<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public void setExposedHeaders(Set<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    @Override
    protected void doFilterInternal(HttpServerRequest request, FilterChain chain) throws Exception {
        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        
        String requestMethod = request.getMethod().getName();
        List<String> requestHeaders = request.getHeaders().get(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);

        HttpServletResponse response = chain.doFilter(request);

        String allowedOriginValue = determineAllowedOrigin(origin);
        if (allowedOriginValue != null) {
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOriginValue);
        }

        if (allowCredentials) {
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }

        if (!exposedHeaders.isEmpty()) {
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, String.join(", ", exposedHeaders));
        }

        if (HttpMethod.OPTIONS.matches(requestMethod)) {
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, formatAllowedMethods());
            response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAge));
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, String.join(", ", requestHeaders));
            }
        }
    }

    private String determineAllowedOrigin(String requestOrigin) {
        if (allowedOrigins.isEmpty()) {
            return allowedOrigin;
        }
        if (allowedOrigins.contains(requestOrigin)) {
            return requestOrigin;
        }
        if (allowedOrigin.equals("*")) {
            return "*";
        }
        return null;
    }

    private String formatAllowedMethods() {
        StringBuilder sb = new StringBuilder();
        for (HttpMethod method : allowedMethods) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(method.getName());
        }
        return sb.toString();
    }
}

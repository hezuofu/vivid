package org.vividframework.http.server;

import org.vividframework.http.*;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Server-side HTTP request interface
 * @author Jon Fisher
 */
public interface HttpServerRequest {

    /**
     * Get unique request ID
     */
    String getId();

    /**
     * Get the HTTP method
     */
    HttpMethod getMethod();

    /**
     * Get request URI
     */
    String getUri();

    /**
     * Get request path (without context path)
     */
    String getPath();

    /**
     * Get query string
     */
    String getQueryString();

    /**
     * Get request headers
     */
    HttpHeaders getHeaders();

    /**
     * Get request body as bytes
     */
    byte[] getBody();

    /**
     * Get request body as string
     */
    String getBodyAsString();

    /**
     * Get request body as string with charset
     */
    String getBodyAsString(Charset charset);

    /**
     * Get parsed form data
     */
    Map<String, List<String>> getFormData();

    /**
     * Get content type
     */
    MediaType getContentType();

    /**
     * Get content length
     */
    long getContentLength();

    /**
     * Get query parameter
     */
    String getQueryParam(String name);

    /**
     * Get all query parameter values
     */
    List<String> getQueryParams(String name);

    /**
     * Get all query parameters as a map
     */
    default Map<String, String[]> getQueryParameters() {
        // Default implementation builds map from query string
        // Subclasses should override for better performance
        Map<String, String[]> params = new java.util.HashMap<>();
        String queryString = getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            for (String pair : queryString.split("&")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    params.computeIfAbsent(key, k -> new String[0]);
                    String[] existing = params.get(key);
                    String[] updated = new String[existing.length + 1];
                    System.arraycopy(existing, 0, updated, 0, existing.length);
                    updated[existing.length] = value;
                    params.put(key, updated);
                }
            }
        }
        return params;
    }

    /**
     * Get request content/body as bytes
     */
    default byte[] getContent() {
        return getBody();
    }

    /**
     * Get path variable
     */
    String getPathVariable(String name);

    /**
     * Get all path variables
     */
    default Map<String, String> getPathVariables() {
        // Default implementation returns empty map
        // Subclasses should override to provide actual path variables
        return Map.of();
    }

    /**
     * Get header value
     */
    String getHeader(String name);

    /**
     * Get all header values
     */
    List<String> getHeaders(String name);

    /**
     * Get cookie by name
     */
    HttpCookie getCookie(String name);

    /**
     * Get all cookies
     */
    Map<String, HttpCookie> getCookies();

    /**
     * Get remote address
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Get local address
     */
    InetSocketAddress getLocalAddress();

    /**
     * Check if request is secure (HTTPS)
     */
    boolean isSecure();

    /**
     * Get request protocol
     */
    String getProtocol();

    /**
     * Check if request is async
     */
    boolean isAsync();

    /**
     * Start async processing
     */
    default AsyncContext startAsync() {
        return new StandardAsyncContext(this);
    }

    /**
     * Get session (if session resolver is available)
     */
    default Object getSession() {
        return null;
    }

    /**
     * Get attribute
     */
    Object getAttribute(String name);

    /**
     * Set attribute
     */
    HttpServerRequest setAttribute(String name, Object value);

    /**
     * Get all attribute names
     */
    List<String> getAttributeNames();

    /**
     * Get request parameter (checks both query params and form data)
     */
    default String getParameter(String name) {
        String value = getQueryParam(name);
        if (value == null) {
            Map<String, List<String>> formData = getFormData();
            List<String> values = formData.get(name);
            if (values != null && !values.isEmpty()) {
                value = values.get(0);
            }
        }
        return value;
    }

    /**
     * Get request parameter values
     */
    default List<String> getParameters(String name) {
        List<String> values = getQueryParams(name);
        if (values == null || values.isEmpty()) {
            Map<String, List<String>> formData = getFormData();
            values = formData.get(name);
        }
        return values != null ? values : List.of();
    }

    /**
     * Get optional attribute
     */
    @SuppressWarnings("unchecked")
    default <T> Optional<T> getAttributeOptional(String name) {
        return Optional.ofNullable((T) getAttribute(name));
    }

    /**
     * Get attribute with default value
     */
    @SuppressWarnings("unchecked")
    default <T> T getAttribute(String name, T defaultValue) {
        Object value = getAttribute(name);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * Get attribute or compute if absent
     */
    @SuppressWarnings("unchecked")
    default <T> T computeIfAbsent(String name, Supplier<T> supplier) {
        T value = (T) getAttribute(name);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                setAttribute(name, value);
            }
        }
        return value;
    }

    /**
     * Async context for async request processing
     */
    interface AsyncContext {
        HttpServerRequest getRequest();
        HttpServletResponse getResponse();
        void complete();
        boolean isCompleted();
        long getTimeout();
        void setTimeout(long timeoutMillis);
        void addListener(AsyncListener listener);
    }

    /**
     * Async listener for async events
     */
    interface AsyncListener {
        default void onStartAsync(AsyncContext context) {}
        default void onComplete(AsyncContext context) {}
        default void onTimeout(AsyncContext context) {}
        default void onError(AsyncContext context, Throwable error) {}
    }

    /**
     * Standard async context implementation
     */
    class StandardAsyncContext implements AsyncContext {
        private final HttpServerRequest request;
        private HttpServletResponse response;
        private long timeout = 30000;
        private boolean completed = false;

        public StandardAsyncContext(HttpServerRequest request) {
            this.request = request;
        }

        @Override
        public HttpServerRequest getRequest() {
            return request;
        }

        @Override
        public HttpServletResponse getResponse() {
            return response;
        }

        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }

        @Override
        public void complete() {
            this.completed = true;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }

        @Override
        public long getTimeout() {
            return timeout;
        }

        @Override
        public void setTimeout(long timeoutMillis) {
            this.timeout = timeoutMillis;
        }

        @Override
        public void addListener(AsyncListener listener) {
            // No-op for basic implementation
        }
    }
}

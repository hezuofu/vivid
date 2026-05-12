package org.vividframework.http;

import org.vividframework.http.server.HttpServerRequest;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable HTTP Request representation
 * @author Jon Fisher
 */
public final class HttpRequest implements HttpServerRequest {

    private final String id;
    private final HttpMethod method;
    private final URI uri;
    private final String path;
    private final String queryString;
    private final HttpHeaders headers;
    private final byte[] body;
    private final Map<String, List<String>> queryParams;
    private final Map<String, List<String>> pathParams;
    private final Map<String, String> cookies;
    private final String remoteAddress;
    private final String localAddress;
    private final int localPort;
    private final String protocol;
    private final Instant requestTime;
    private final Map<String, Object> attributes;

    private HttpRequest(Builder builder) {
        this.id = builder.id;
        this.method = builder.method;
        this.uri = builder.uri;
        this.path = builder.path;
        this.queryString = builder.queryString;
        this.headers = builder.headers;
        this.body = builder.body;
        this.queryParams = builder.queryParams;
        this.pathParams = builder.pathParams;
        this.cookies = builder.cookies;
        this.remoteAddress = builder.remoteAddress;
        this.localAddress = builder.localAddress;
        this.localPort = builder.localPort;
        this.protocol = builder.protocol;
        this.requestTime = builder.requestTime;
        this.attributes = builder.attributes;
    }

    // ========== Core getters ==========

    @Override
    public String getId() {
        return id;
    }

    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public String getUri() {
        return uri != null ? uri.toString() : path;
    }

    /**
     * Get URI as java.net.URI
     */
    public URI getUriObject() {
        return uri;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public String getBodyAsString() {
        return getBodyAsString(StandardCharsets.UTF_8);
    }

    @Override
    public String getBodyAsString(Charset charset) {
        if (body == null || body.length == 0) {
            return "";
        }
        return new String(body, charset);
    }

    @Override
    public Map<String, List<String>> getFormData() {
        return Map.of();
    }

    @Override
    public MediaType getContentType() {
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
        return contentType != null ? MediaType.parse(contentType) : null;
    }

    @Override
    public long getContentLength() {
        return headers.getContentLength();
    }

    @Override
    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    @Override
    public List<String> getQueryParams(String name) {
        List<String> values = queryParams.get(name);
        return values != null ? values : List.of();
    }

    @Override
    public String getPathVariable(String name) {
        List<String> values = pathParams.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    @Override
    public String getHeader(String name) {
        return headers.getFirst(name);
    }

    @Override
    public List<String> getHeaders(String name) {
        return headers.get(name);
    }

    @Override
    public HttpCookie getCookie(String name) {
        String value = cookies.get(name);
        if (value != null) {
            return HttpCookie.of(name, value);
        }
        return null;
    }

    @Override
    public Map<String, HttpCookie> getCookies() {
        Map<String, HttpCookie> result = new HashMap<>();
        cookies.forEach((n, v) -> result.put(n, HttpCookie.of(n, v)));
        return result;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        if (remoteAddress == null) {
            return null;
        }
        return new InetSocketAddress(remoteAddress, 0);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        if (localAddress == null) {
            return null;
        }
        return new InetSocketAddress(localAddress, localPort);
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(uri != null ? uri.getScheme() : null) 
            || "wss".equalsIgnoreCase(uri != null ? uri.getScheme() : null);
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public AsyncContext startAsync() {
        return new StandardAsyncContext(this);
    }

    @Override
    public Object getSession() {
        return null;
    }

    // ========== Attribute methods ==========

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public HttpRequest setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public List<String> getAttributeNames() {
        return new ArrayList<>(attributes.keySet());
    }

    // ========== Additional convenience methods ==========

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public Map<String, List<String>> getPathParams() {
        return pathParams;
    }

    /**
     * Get cookies as string map
     */
    public Map<String, String> getCookiesMap() {
        return cookies;
    }

    /**
     * Get remote address as string
     */
    public String getRemoteAddressString() {
        return remoteAddress;
    }

    /**
     * Get local address as string
     */
    public String getLocalAddressString() {
        return localAddress;
    }

    public int getLocalPort() {
        return localPort;
    }

    public Instant getRequestTime() {
        return requestTime;
    }

    public String getHost() {
        return uri != null ? uri.getHost() : null;
    }

    public int getPort() {
        int port = uri != null ? uri.getPort() : -1;
        if (port == -1) {
            return isSecure() ? 443 : 80;
        }
        return port;
    }

    public String getContentTypeValue() {
        return headers.getFirst(HttpHeaders.CONTENT_TYPE);
    }

    public String getParameter(String name) {
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

    public List<String> getParameters(String name) {
        List<String> values = getQueryParams(name);
        if (values == null || values.isEmpty()) {
            values = getFormData().get(name);
        }
        return values != null ? values : List.of();
    }

    /**
     * Get cookie value as string
     */
    public String getCookieValue(String name) {
        return cookies.get(name);
    }

    public boolean hasPathVariable(String name) {
        return pathParams.containsKey(name);
    }

    public String getScheme() {
        return uri != null ? uri.getScheme() : null;
    }

    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HttpRequest of(String method, String uri) {
        return builder()
                .method(HttpMethod.resolve(method))
                .uri(URI.create(uri))
                .build();
    }

    public static class Builder {
        private String id;
        private HttpMethod method;
        private URI uri;
        private String path;
        private String queryString;
        private HttpHeaders headers = new HttpHeaders();
        private byte[] body;
        private Map<String, List<String>> queryParams = Map.of();
        private Map<String, List<String>> pathParams = Map.of();
        private Map<String, String> cookies = Map.of();
        private String remoteAddress;
        private String localAddress;
        private int localPort;
        private String protocol = "HTTP/1.1";
        private Instant requestTime = Instant.now();
        private Map<String, Object> attributes = new ConcurrentHashMap<>();

        public Builder() {}

        public Builder(HttpRequest request) {
            this.id = request.id;
            this.method = request.method;
            this.uri = request.uri;
            this.path = request.path;
            this.queryString = request.queryString;
            this.headers = request.headers;
            this.body = request.body;
            this.queryParams = request.queryParams;
            this.pathParams = request.pathParams;
            this.cookies = request.cookies;
            this.remoteAddress = request.remoteAddress;
            this.localAddress = request.localAddress;
            this.localPort = request.localPort;
            this.protocol = request.protocol;
            this.requestTime = request.requestTime;
            this.attributes = new ConcurrentHashMap<>(request.attributes);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder method(String method) {
            this.method = HttpMethod.resolve(method);
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            this.path = uri != null ? uri.getPath() : null;
            this.queryString = uri != null ? uri.getQuery() : null;
            return this;
        }

        public Builder uri(String uri) {
            return uri(URI.create(uri));
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder headers(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(byte[] body) {
            this.body = body;
            return this;
        }

        public Builder queryParams(Map<String, List<String>> queryParams) {
            this.queryParams = queryParams != null ? queryParams : Map.of();
            return this;
        }

        public Builder pathParams(Map<String, List<String>> pathParams) {
            this.pathParams = pathParams != null ? pathParams : Map.of();
            return this;
        }

        public Builder cookies(Map<String, String> cookies) {
            this.cookies = cookies != null ? cookies : Map.of();
            return this;
        }

        public Builder remoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder localAddress(String localAddress) {
            this.localAddress = localAddress;
            return this;
        }

        public Builder localPort(int localPort) {
            this.localPort = localPort;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder requestTime(Instant requestTime) {
            this.requestTime = requestTime;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append(" ").append(path);
        if (queryString != null) {
            sb.append("?").append(queryString);
        }
        sb.append(" ").append(protocol);
        return sb.toString();
    }
}

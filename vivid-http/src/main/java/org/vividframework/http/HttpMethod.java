package org.vividframework.http;

/**
 * HTTP method enumeration
 * @author Jon Fisher
 */
public enum HttpMethod {
    GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE;

    private final String name;

    HttpMethod() {
        this.name = name();
    }

    public String getName() {
        return name;
    }

    public boolean matches(String method) {
        return name().equalsIgnoreCase(method);
    }

    public static HttpMethod resolve(String method) {
        if (method == null) {
            return null;
        }
        try {
            return HttpMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

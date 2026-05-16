package org.vividframework.http;

import java.util.Objects;

/**
 * Immutable HTTP Cookie implementation
 * @author sketch
 */
public final class HttpCookie {

    private final String name;
    private final String value;
    private final String domain;
    private final String path;
    private final long maxAge;
    private final boolean secure;
    private final boolean httpOnly;
    private final String sameSite;

    private HttpCookie(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.domain = builder.domain;
        this.path = builder.path;
        this.maxAge = builder.maxAge;
        this.secure = builder.secure;
        this.httpOnly = builder.httpOnly;
        this.sameSite = builder.sameSite;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public String getSameSite() {
        return sameSite;
    }

    public boolean isExpired() {
        return maxAge == 0;
    }

    public boolean hasMaxAge() {
        return maxAge > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        if (domain != null) {
            sb.append("; Domain=").append(domain);
        }
        if (path != null) {
            sb.append("; Path=").append(path);
        }
        if (maxAge >= 0) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (secure) {
            sb.append("; Secure");
        }
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        if (sameSite != null) {
            sb.append("; SameSite=").append(sameSite);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpCookie that = (HttpCookie) o;
        return Objects.equals(name, that.name) && Objects.equals(domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, domain);
    }

    public static Builder builder(String name, String value) {
        return new Builder(name, value);
    }

    public static HttpCookie of(String name, String value) {
        return builder(name, value).build();
    }

    public static HttpCookie from(String name, String value) {
        return builder(name, value).build();
    }

    public static HttpCookie parse(String header) {
        if (header == null || header.isEmpty()) {
            return null;
        }
        String name = null;
        String value = null;
        String domain = null;
        String path = null;
        long maxAge = -1;
        boolean secure = false;
        boolean httpOnly = false;

        String[] parts = header.split(";");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            int equalsIndex = part.indexOf('=');
            if (equalsIndex > 0) {
                String key = part.substring(0, equalsIndex).trim().toLowerCase();
                String val = part.substring(equalsIndex + 1).trim();
                if (i == 0) {
                    name = key;
                    value = val;
                } else {
                    switch (key) {
                        case "domain": domain = val; break;
                        case "path": path = val; break;
                        case "max-age": maxAge = Long.parseLong(val); break;
                        case "secure": secure = true; break;
                        case "httponly": httpOnly = true; break;
                        case "samesite": break; // Handle if needed
                    }
                }
            }
        }

        if (name == null) return null;
        return builder(name, value)
                .domain(domain)
                .path(path)
                .maxAge(maxAge)
                .secure(secure)
                .httpOnly(httpOnly)
                .build();
    }

    public static class Builder {
        private final String name;
        private final String value;
        private String domain;
        private String path;
        private long maxAge = -1;
        private boolean secure = false;
        private boolean httpOnly = false;
        private String sameSite;

        public Builder(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder maxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public Builder sameSite(String sameSite) {
            this.sameSite = sameSite;
            return this;
        }

        public HttpCookie build() {
            return new HttpCookie(this);
        }
    }
}

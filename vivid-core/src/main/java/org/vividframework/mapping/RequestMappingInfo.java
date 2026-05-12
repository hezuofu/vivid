package org.vividframework.mapping;

import org.vividframework.http.HttpMethod;

import java.util.*;

/**
 * Request mapping information containing path patterns and HTTP methods
 * @author Jon Fisher
 */
public class RequestMappingInfo {

    private String name;
    private Set<String> patterns = new HashSet<>();
    private Set<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
    private Set<String> headers = new HashSet<>();
    private Set<String> consumes = new HashSet<>();
    private Set<String> produces = new HashSet<>();
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> attributes = new HashMap<>();
    private RequestMappingInfo.BuilderOptions options = new RequestMappingInfo.BuilderOptions();

    public RequestMappingInfo() {
    }

    public RequestMappingInfo(String... patterns) {
        this.patterns.addAll(Arrays.asList(patterns));
    }

    public String getName() {
        return name;
    }

    public RequestMappingInfo setName(String name) {
        this.name = name;
        return this;
    }

    public Set<String> getPatterns() {
        return patterns;
    }

    public RequestMappingInfo setPatterns(Set<String> patterns) {
        this.patterns = patterns != null ? patterns : new HashSet<>();
        return this;
    }

    public RequestMappingInfo patterns(String... patterns) {
        this.patterns.addAll(Arrays.asList(patterns));
        return this;
    }

    public Set<HttpMethod> getMethods() {
        return methods;
    }

    public RequestMappingInfo setMethods(Set<HttpMethod> methods) {
        this.methods = methods != null ? methods : EnumSet.noneOf(HttpMethod.class);
        return this;
    }

    public RequestMappingInfo methods(HttpMethod... methods) {
        this.methods.addAll(Arrays.asList(methods));
        return this;
    }

    public Set<String> getHeaders() {
        return headers;
    }

    public RequestMappingInfo setHeaders(Set<String> headers) {
        this.headers = headers != null ? headers : new HashSet<>();
        return this;
    }

    public Set<String> getConsumes() {
        return consumes;
    }

    public RequestMappingInfo setConsumes(Set<String> consumes) {
        this.consumes = consumes != null ? consumes : new HashSet<>();
        return this;
    }

    public Set<String> getProduces() {
        return produces;
    }

    public RequestMappingInfo setProduces(Set<String> produces) {
        this.produces = produces != null ? produces : new HashSet<>();
        return this;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public RequestMappingInfo setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public BuilderOptions getOptions() {
        return options;
    }

    public boolean hasPatterns() {
        return !patterns.isEmpty();
    }

    public boolean hasMethods() {
        return !methods.isEmpty();
    }

    public boolean hasHeaders() {
        return !headers.isEmpty();
    }

    public boolean hasConsumes() {
        return !consumes.isEmpty();
    }

    public boolean hasProduces() {
        return !produces.isEmpty();
    }

    /**
     * Check if this mapping matches the given conditions
     */
    public boolean matches(String pattern, HttpMethod method) {
        if (!patterns.isEmpty() && !patterns.contains(pattern)) {
            return false;
        }
        if (!methods.isEmpty() && method != null && !methods.contains(method)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!methods.isEmpty()) {
            sb.append(methods).append(" ");
        }
        if (!patterns.isEmpty()) {
            sb.append(patterns);
        }
        return sb.toString().trim();
    }

    /**
     * Builder options
     */
    public static class BuilderOptions {
        private boolean customRegistrar = false;
        private boolean handlerMethod = true;
        private boolean activeProfiles = false;

        public boolean isCustomRegistrar() {
            return customRegistrar;
        }

        public BuilderOptions setCustomRegistrar(boolean customRegistrar) {
            this.customRegistrar = customRegistrar;
            return this;
        }

        public boolean isHandlerMethod() {
            return handlerMethod;
        }

        public BuilderOptions setHandlerMethod(boolean handlerMethod) {
            this.handlerMethod = handlerMethod;
            return this;
        }

        public boolean isActiveProfiles() {
            return activeProfiles;
        }

        public BuilderOptions setActiveProfiles(boolean activeProfiles) {
            this.activeProfiles = activeProfiles;
            return this;
        }
    }

    /**
     * Builder for RequestMappingInfo
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private final Set<String> patterns = new HashSet<>();
        private final Set<HttpMethod> methods = EnumSet.noneOf(HttpMethod.class);
        private final Set<String> headers = new HashSet<>();
        private final Set<String> consumes = new HashSet<>();
        private final Set<String> produces = new HashSet<>();
        private final Map<String, Object> params = new HashMap<>();
        private final Map<String, Object> attributes = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder patterns(String... patterns) {
            this.patterns.addAll(Arrays.asList(patterns));
            return this;
        }

        public Builder methods(HttpMethod... methods) {
            this.methods.addAll(Arrays.asList(methods));
            return this;
        }

        public Builder headers(String... headers) {
            this.headers.addAll(Arrays.asList(headers));
            return this;
        }

        public Builder consumes(String... consumes) {
            this.consumes.addAll(Arrays.asList(consumes));
            return this;
        }

        public Builder produces(String... produces) {
            this.produces.addAll(Arrays.asList(produces));
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params.putAll(params);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.putAll(attributes);
            return this;
        }

        public Builder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public RequestMappingInfo build() {
            RequestMappingInfo info = new RequestMappingInfo();
            info.name = this.name;
            info.patterns = new HashSet<>(this.patterns);
            info.methods = EnumSet.copyOf(this.methods);
            info.headers = new HashSet<>(this.headers);
            info.consumes = new HashSet<>(this.consumes);
            info.produces = new HashSet<>(this.produces);
            info.params = new HashMap<>(this.params);
            info.attributes = new HashMap<>(this.attributes);
            return info;
        }
    }
}

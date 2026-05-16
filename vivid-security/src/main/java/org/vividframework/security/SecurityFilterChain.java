package org.vividframework.security;

import org.vividframework.web.filter.Filter;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Security filter chain for protecting requests
 * @author sketch
 */
public class SecurityFilterChain {

    private final List<Filter> filters = new ArrayList<>();
    private boolean permitAll = false;
    private boolean denyAll = false;

    public SecurityFilterChain() {
    }

    public SecurityFilterChain(List<Filter> filters) {
        this.filters.addAll(filters);
    }

    public SecurityFilterChain addFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    public SecurityFilterChain addFilterAt(Filter filter, int position) {
        this.filters.add(position, filter);
        return this;
    }

    public SecurityFilterChain permitAll() {
        this.permitAll = true;
        return this;
    }

    public SecurityFilterChain denyAll() {
        this.denyAll = true;
        return this;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public boolean isPermitAll() {
        return permitAll;
    }

    public boolean isDenyAll() {
        return denyAll;
    }

    /**
     * Check if this chain matches the request
     */
    public boolean matches(HttpServerRequest request) {
        return !isEmpty() || isPermitAll() || isDenyAll();
    }

    /**
     * Create a default security filter chain
     */
    public static SecurityFilterChainBuilder builder() {
        return new SecurityFilterChainBuilder();
    }

    public static class SecurityFilterChainBuilder {
        private final List<Filter> filters = new ArrayList<>();
        private boolean permitAll = false;
        private boolean denyAll = false;

        public SecurityFilterChainBuilder addFilter(Filter filter) {
            this.filters.add(filter);
            return this;
        }

        public SecurityFilterChainBuilder addFilter(Filter filter, int position) {
            this.filters.add(position, filter);
            return this;
        }

        public SecurityFilterChainBuilder permitAll() {
            this.permitAll = true;
            return this;
        }

        public SecurityFilterChainBuilder denyAll() {
            this.denyAll = true;
            return this;
        }

        public SecurityFilterChain build() {
            SecurityFilterChain chain = new SecurityFilterChain(filters);
            if (permitAll) chain.permitAll();
            if (denyAll) chain.denyAll();
            return chain;
        }
    }
}

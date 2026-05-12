package org.vividframework.filter;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.server.HttpServerRequest;

/**
 * Filter chain interface for executing filters in order
 * @author Jon Fisher
 */
public interface FilterChain {

    /**
     * Execute the next filter in the chain
     * @param request The request
     * @return The response
     * @throws Exception if any error occurs
     */
    HttpServletResponse doFilter(HttpServerRequest request) throws Exception;

    /**
     * Get remaining filters count
     */
    int remainingFilters();

    /**
     * Check if chain is exhausted
     */
    boolean isExhausted();

    /**
     * Create a filter chain from a list of filters
     */
    static FilterChain create(Filter... filters) {
        return create(java.util.Arrays.asList(filters));
    }

    /**
     * Create a filter chain from a list of filters
     */
    static FilterChain create(java.util.List<Filter> filters) {
        return new DefaultFilterChain(filters);
    }

    /**
     * Create an empty filter chain
     */
    static FilterChain empty() {
        return new FilterChain() {
            @Override
            public HttpServletResponse doFilter(HttpServerRequest request) throws Exception {
                throw new IllegalStateException("Filter chain exhausted without response");
            }

            @Override
            public int remainingFilters() {
                return 0;
            }

            @Override
            public boolean isExhausted() {
                return true;
            }
        };
    }

    /**
     * Create a terminal filter chain with a handler
     */
    static FilterChain of(java.util.function.BiFunction<HttpServerRequest, FilterChain, HttpServletResponse> terminal) {
        return new FilterChain() {
            private FilterChain delegate = empty();

            @Override
            public HttpServletResponse doFilter(HttpServerRequest request) throws Exception {
                return delegate.doFilter(request);
            }

            @Override
            public int remainingFilters() {
                return 0;
            }

            @Override
            public boolean isExhausted() {
                return true;
            }
        };
    }

    /**
     * Default filter chain implementation
     */
    class DefaultFilterChain implements FilterChain {
        private final java.util.List<Filter> filters;
        private int index = 0;

        public DefaultFilterChain(java.util.List<Filter> filters) {
            this.filters = new java.util.ArrayList<>(filters);
            this.filters.sort((f1, f2) -> {
                int o1 = f1.getOrder();
                int o2 = f2.getOrder();
                return Integer.compare(o1, o2);
            });
        }

        @Override
        public HttpServletResponse doFilter(HttpServerRequest request) throws Exception {
            if (index >= filters.size()) {
                throw new IllegalStateException("Filter chain exhausted without response");
            }
            Filter filter = filters.get(index++);
            return filter.doFilter(request, this);
        }

        @Override
        public int remainingFilters() {
            return filters.size() - index;
        }

        @Override
        public boolean isExhausted() {
            return index >= filters.size();
        }
    }
}

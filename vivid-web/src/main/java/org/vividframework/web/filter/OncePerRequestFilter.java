package org.vividframework.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;

/**
 * Base class for filters that should only execute once per request
 * @author Jon Fisher
 */
public abstract class OncePerRequestFilter implements Filter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ALREADY_FILTERED_SUFFIX = ".FILTERED";

    @Override
    public final HttpServletResponse doFilter(HttpServerRequest request, FilterChain chain) throws Exception {
        String name = getName();
        String alreadyFilteredAttributeName = name + ALREADY_FILTERED_SUFFIX;

        if (request.getAttribute(alreadyFilteredAttributeName) != null) {
            logger.trace("Filter '{}' already executed for request '{}', skipping", name, request.getPath());
            chain.doFilter(request);
        } else {
            request.setAttribute(alreadyFilteredAttributeName, Boolean.TRUE);
            try {
                doFilterInternal(request, chain);
            } finally {
                request.setAttribute(alreadyFilteredAttributeName, null);
            }
        }
        return null;
    }

    /**
     * Same contract as {@link #doFilter}, but guaranteed to be
     * executed only once per request.
     */
    protected abstract void doFilterInternal(HttpServerRequest request, FilterChain chain) throws Exception;

    /**
     * Should this filter execute for an async dispatch?
     */
    protected boolean shouldNotFilterAsync() {
        return false;
    }

    /**
     * Should this filter be applied to error pages?
     */
    protected boolean shouldNotFilterError() {
        return false;
    }
}

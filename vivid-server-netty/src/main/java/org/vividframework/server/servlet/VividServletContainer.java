package org.vividframework.server.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight Servlet Container on Netty.
 * Manages servlet/filter registrations, dispatches requests, handles lifecycle.
 *
 * @author sketch
 */
public class VividServletContainer {

    private static final Logger logger = LoggerFactory.getLogger(VividServletContainer.class);

    private final VividServletContext servletContext = new VividServletContext();
    private final Map<String, ServletHolder> servlets = new LinkedHashMap<>();
    private final Map<String, FilterHolder> filters = new LinkedHashMap<>();
    private final Map<String, VividHttpSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> servletMappings = new LinkedHashMap<>(); // urlPattern → servletName
    private final Map<String, List<String>> filterMappings = new LinkedHashMap<>(); // urlPattern → filterNames
    private boolean initialized;

    /**
     * Add a servlet with a URL mapping.
     */
    public VividServletContainer addServlet(String name, HttpServlet servlet, String... urlPatterns) {
        servlets.put(name, new ServletHolder(name, servlet));
        for (String pattern : urlPatterns) {
            servletMappings.put(pattern, name);
        }
        return this;
    }

    /**
     * Add a servlet class with a URL mapping.
     */
    public VividServletContainer addServlet(String name, Class<? extends HttpServlet> servletClass,
                                             String... urlPatterns) {
        try {
            HttpServlet servlet = servletClass.getDeclaredConstructor().newInstance();
            addServlet(name, servlet, urlPatterns);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate servlet: " + servletClass.getName(), e);
        }
        return this;
    }

    /**
     * Add a filter with URL patterns.
     */
    public VividServletContainer addFilter(String name, Filter filter, String... urlPatterns) {
        filters.put(name, new FilterHolder(name, filter));
        for (String pattern : urlPatterns) {
            filterMappings.computeIfAbsent(pattern, k -> new ArrayList<>()).add(name);
        }
        return this;
    }

    /**
     * Add a filter class with URL patterns.
     */
    public VividServletContainer addFilter(String name, Class<? extends Filter> filterClass,
                                            String... urlPatterns) {
        try {
            Filter filter = filterClass.getDeclaredConstructor().newInstance();
            addFilter(name, filter, urlPatterns);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate filter: " + filterClass.getName(), e);
        }
        return this;
    }

    /**
     * Get or create a session.
     */
    public VividHttpSession getSession(String sessionId, boolean create) {
        if (sessionId != null) {
            VividHttpSession session = sessions.get(sessionId);
            if (session != null) {
                if (session.isValid()) {
                    session.access();
                    session.markNotNew();
                    return session;
                }
                sessions.remove(sessionId);
            }
        }
        if (create) {
            String id = UUID.randomUUID().toString().replace("-", "");
            VividHttpSession session = new VividHttpSession(id, servletContext);
            sessions.put(id, session);
            return session;
        }
        return null;
    }

    /**
     * Clean up expired sessions.
     */
    public void cleanupSessions() {
        sessions.entrySet().removeIf(e -> !e.getValue().isValid());
    }

    /**
     * Initialize all servlets and filters.
     */
    public void init() throws ServletException {
        if (initialized) return;

        // Init filters
        for (FilterHolder holder : filters.values()) {
            holder.filter.init(new VividFilterConfig(holder.name, servletContext));
            logger.debug("Initialized filter: {}", holder.name);
        }

        // Init servlets
        for (ServletHolder holder : servlets.values()) {
            holder.servlet.init(new VividServletConfig(holder.name, servletContext));
            logger.debug("Initialized servlet: {}", holder.name);
        }

        initialized = true;
        logger.info("VividServletContainer initialized: {} servlets, {} filters",
                servlets.size(), filters.size());
    }

    /**
     * Destroy all servlets and filters.
     */
    public void destroy() {
        for (ServletHolder holder : servlets.values()) {
            try { holder.servlet.destroy(); } catch (Exception ignored) {}
        }
        for (FilterHolder holder : filters.values()) {
            try { holder.filter.destroy(); } catch (Exception ignored) {}
        }
        servlets.clear();
        filters.clear();
        initialized = false;
    }

    /**
     * Dispatch a request through the filter chain to the matching servlet.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Find matching servlet
        ServletHolder servlet = findServlet(path);
        if (servlet == null) {
            response.sendError(404, "No servlet mapped for " + path);
            return;
        }

        // Build filter chain
        List<Filter> matchedFilters = findFilters(path);

        if (matchedFilters.isEmpty()) {
            servlet.servlet.service(request, response);
        } else {
            FilterChain chain = new ServletFilterChain(matchedFilters, servlet.servlet);
            chain.doFilter(request, response);
        }
    }

    private ServletHolder findServlet(String path) {
        // Exact match
        String name = servletMappings.get(path);
        if (name != null) return servlets.get(name);

        // Prefix match
        for (Map.Entry<String, String> entry : servletMappings.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (path.startsWith(prefix)) {
                    return servlets.get(entry.getValue());
                }
            }
            if (pattern.endsWith("*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);
                if (path.startsWith(prefix)) {
                    return servlets.get(entry.getValue());
                }
            }
        }

        // Default servlet
        return servlets.get(servletMappings.get("/"));
    }

    private List<Filter> findFilters(String path) {
        List<Filter> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : filterMappings.entrySet()) {
            String pattern = entry.getKey();
            if (path.equals(pattern) ||
                    (pattern.endsWith("/*") && path.startsWith(pattern.substring(0, pattern.length() - 2))) ||
                    (pattern.endsWith("*") && path.startsWith(pattern.substring(0, pattern.length() - 1)))) {
                for (String filterName : entry.getValue()) {
                    FilterHolder holder = filters.get(filterName);
                    if (holder != null) result.add(holder.filter);
                }
            }
        }
        return result;
    }

    public VividServletContext getServletContext() { return servletContext; }

    // --- Inner classes ---

    static class ServletHolder {
        final String name; final HttpServlet servlet;
        ServletHolder(String name, HttpServlet servlet) { this.name = name; this.servlet = servlet; }
    }

    static class FilterHolder {
        final String name; final Filter filter;
        FilterHolder(String name, Filter filter) { this.name = name; this.filter = filter; }
    }

    /**
     * Servlet FilterChain that delegates to a target servlet.
     */
    static class ServletFilterChain implements FilterChain {
        private final List<Filter> filters;
        private final Servlet servlet;
        private int index;

        ServletFilterChain(List<Filter> filters, Servlet servlet) {
            this.filters = filters;
            this.servlet = servlet;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response)
                throws IOException, ServletException {
            if (index < filters.size()) {
                filters.get(index++).doFilter(request, response, this);
            } else {
                servlet.service(request, response);
            }
        }
    }

    /**
     * Minimal ServletConfig implementation.
     */
    static class VividServletConfig implements ServletConfig {
        private final String name;
        private final ServletContext context;
        private final Map<String, String> initParams = new HashMap<>();

        VividServletConfig(String name, ServletContext context) { this.name = name; this.context = context; }
        @Override public String getServletName() { return name; }
        @Override public ServletContext getServletContext() { return context; }
        @Override public String getInitParameter(String n) { return initParams.get(n); }
        @Override public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParams.keySet()); }
        public void setInitParameter(String n, String v) { initParams.put(n, v); }
    }

    /**
     * Minimal FilterConfig implementation.
     */
    static class VividFilterConfig implements FilterConfig {
        private final String name;
        private final ServletContext context;
        VividFilterConfig(String name, ServletContext context) { this.name = name; this.context = context; }
        @Override public String getFilterName() { return name; }
        @Override public ServletContext getServletContext() { return context; }
        @Override public String getInitParameter(String n) { return null; }
        @Override public Enumeration<String> getInitParameterNames() { return Collections.emptyEnumeration(); }
    }
}

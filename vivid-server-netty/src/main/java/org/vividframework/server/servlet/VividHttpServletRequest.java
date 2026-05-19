package org.vividframework.server.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServerRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

/**
 * Jakarta Servlet HttpServletRequest implementation backed by Vivid's HttpServerRequest.
 * @author sketch
 */
public class VividHttpServletRequest implements HttpServletRequest {

    private final HttpServerRequest request;
    private final VividServletContext servletContext;
    private final Map<String, Object> attributes = new HashMap<>();
    private final String scheme;
    private final String serverName;
    private final int serverPort;
    private String characterEncoding = "UTF-8";

    public VividHttpServletRequest(HttpServerRequest request, VividServletContext servletContext,
                                    String scheme, String serverName, int serverPort) {
        this.request = request;
        this.servletContext = servletContext;
        this.scheme = scheme;
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    @Override public String getAuthType() { return null; }
    @Override public Cookie[] getCookies() {
        Map<String, org.vividframework.http.HttpCookie> cookies = request.getCookies();
        if (cookies.isEmpty()) return new Cookie[0];
        Cookie[] result = new Cookie[cookies.size()];
        int i = 0;
        for (Map.Entry<String, org.vividframework.http.HttpCookie> e : cookies.entrySet()) {
            Cookie c = new Cookie(e.getKey(), e.getValue().getValue());
            result[i++] = c;
        }
        return result;
    }
    @Override public long getDateHeader(String name) { return request.getHeaders().getDate(); }
    @Override public String getHeader(String name) { return request.getHeader(name); }
    @Override public Enumeration<String> getHeaders(String name) {
        List<String> values = request.getHeaders().get(name);
        return Collections.enumeration(values);
    }
    @Override public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.getHeaders().keySet());
    }
    @Override public int getIntHeader(String name) {
        String v = getHeader(name);
        return v != null ? Integer.parseInt(v) : -1;
    }
    @Override public String getMethod() { return request.getMethod().name(); }
    @Override public String getPathInfo() { return null; }
    @Override public String getPathTranslated() { return null; }
    @Override public String getContextPath() { return ""; }
    @Override public String getQueryString() { return request.getQueryString(); }
    @Override public String getRemoteUser() { return null; }
    @Override public boolean isUserInRole(String role) { return false; }
    @Override public Principal getUserPrincipal() {
        return (Principal) attributes.get("principal");
    }
    @Override public String getRequestedSessionId() { return null; }
    @Override public String getRequestURI() { return request.getPath(); }
    @Override public StringBuffer getRequestURL() {
        return new StringBuffer(scheme + "://" + serverName + ":" + serverPort + request.getPath());
    }
    @Override public String getServletPath() { return request.getPath(); }
    @Override public HttpSession getSession(boolean create) {
        return null; // Session not yet implemented
    }
    @Override public HttpSession getSession() { return getSession(true); }
    @Override public String changeSessionId() { return null; }
    @Override public boolean isRequestedSessionIdValid() { return false; }
    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
    @Override public boolean isRequestedSessionIdFromURL() { return false; }
    @Override public boolean authenticate(HttpServletResponse r) { return false; }
    @Override public void login(String u, String p) {}
    @Override public void logout() {}
    @Override public Collection<Part> getParts() { return List.of(); }
    @Override public Part getPart(String name) { return null; }
    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> c) { return null; }

    @Override public Object getAttribute(String name) { return attributes.get(name); }
    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
    @Override public String getCharacterEncoding() { return characterEncoding; }
    @Override public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;
    }
    @Override public int getContentLength() { return (int) request.getContentLength(); }
    @Override public long getContentLengthLong() { return request.getContentLength(); }
    @Override public String getContentType() {
        return request.getHeader(HttpHeaders.CONTENT_TYPE);
    }
    @Override public ServletInputStream getInputStream() {
        byte[] body = request.getContent();
        if (body == null) body = new byte[0];
        return new VividServletInputStream(new ByteArrayInputStream(body));
    }
    @Override public String getParameter(String name) {
        return request.getQueryParam(name);
    }
    @Override public Enumeration<String> getParameterNames() {
        return Collections.enumeration(request.getQueryParameters().keySet());
    }
    @Override public String[] getParameterValues(String name) {
        List<String> values = request.getQueryParams(name);
        return values.toArray(new String[0]);
    }
    @Override public Map<String, String[]> getParameterMap() {
        return request.getQueryParameters();
    }
    @Override public String getProtocol() { return "HTTP/1.1"; }

    @Override public String getScheme() { return scheme; }
    @Override public String getServerName() { return serverName; }
    @Override public int getServerPort() { return serverPort; }
    @Override public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
    @Override public String getRemoteAddr() { return request.getRemoteAddress().getHostString(); }
    @Override public String getRemoteHost() { return request.getRemoteAddress().getHostString(); }
    @Override public void setAttribute(String name, Object o) { attributes.put(name, o); }
    @Override public void removeAttribute(String name) { attributes.remove(name); }
    @Override public Locale getLocale() { return Locale.getDefault(); }
    @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(List.of(Locale.getDefault())); }
    @Override public boolean isSecure() { return "https".equals(scheme); }
    @Override public RequestDispatcher getRequestDispatcher(String path) {
        return new VividRequestDispatcher(path, servletContext);
    }
    @Override public int getRemotePort() { return 0; }
    @Override public String getLocalName() { return serverName; }
    @Override public String getLocalAddr() { return request.getLocalAddress().getHostString(); }
    @Override public int getLocalPort() { return serverPort; }
    @Override public ServletContext getServletContext() { return servletContext; }
    @Override public AsyncContext startAsync() { throw new UnsupportedOperationException(); }
    @Override public AsyncContext startAsync(ServletRequest r, ServletResponse res) { throw new UnsupportedOperationException(); }
    @Override public boolean isAsyncStarted() { return false; }
    @Override public boolean isAsyncSupported() { return false; }
    @Override public AsyncContext getAsyncContext() { throw new UnsupportedOperationException(); }
    @Override public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }
    @Override public String getRequestId() { return request.getId(); }
    @Override public String getProtocolRequestId() { return request.getId(); }
    @Override public ServletConnection getServletConnection() { return null; }

    /** Access the underlying Vivid request. */
    public HttpServerRequest getVividRequest() { return request; }
}

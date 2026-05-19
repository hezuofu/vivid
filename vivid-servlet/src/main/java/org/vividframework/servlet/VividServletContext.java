package org.vividframework.servlet;

import jakarta.servlet.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import org.vividframework.context.GenericApplicationContext;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Jakarta Servlet ServletContext implementation.
 * @author sketch
 */
public class VividServletContext implements ServletContext {

    private final GenericApplicationContext applicationContext;
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String> initParams = new HashMap<>();
    private String contextPath = "";
    private int majorVersion = 6;
    private int minorVersion = 0;

    public VividServletContext(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override public String getContextPath() { return contextPath; }
    public void setContextPath(String p) { contextPath = p; }
    @Override public ServletContext getContext(String path) { return this; }
    @Override public int getMajorVersion() { return majorVersion; }
    @Override public int getMinorVersion() { return minorVersion; }
    @Override public int getEffectiveMajorVersion() { return majorVersion; }
    @Override public int getEffectiveMinorVersion() { return minorVersion; }
    @Override public String getMimeType(String file) {
        return org.vividframework.web.StaticResourceHandler.getContentType(file);
    }
    @Override public Set<String> getResourcePaths(String path) { return Set.of(); }
    @Override public URL getResource(String path) throws MalformedURLException {
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }
    @Override public InputStream getResourceAsStream(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
    @Override public RequestDispatcher getRequestDispatcher(String path) {
        return new VividRequestDispatcher(path, this);
    }
    @Override public RequestDispatcher getNamedDispatcher(String name) { return null; }
    @Override public String getServletContextName() { return "Vivid Servlet"; }
    @Override public String getServerInfo() { return "Vivid/1.0"; }

    @Override public void setAttribute(String name, Object o) { attributes.put(name, o); }
    @Override public Object getAttribute(String name) { return attributes.get(name); }
    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
    @Override public void removeAttribute(String name) { attributes.remove(name); }

    @Override public String getInitParameter(String name) { return initParams.get(name); }
    @Override public Enumeration<String> getInitParameterNames() { return Collections.enumeration(initParams.keySet()); }
    @Override public boolean setInitParameter(String name, String value) {
        initParams.put(name, value); return true;
    }

    @Override public void log(String msg) { System.getLogger("vivid.servlet").log(System.Logger.Level.INFO, msg); }
    public void log(Exception e, String msg) { log(msg + ": " + e); }
    @Override public void log(String msg, Throwable t) { log(msg + ": " + t); }

    @Override public String getRealPath(String path) { return null; }

    // --- Unsupported / no-op ---
    @Override public ServletRegistration.Dynamic addServlet(String n, String c) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String n, Servlet s) { return null; }
    @Override public ServletRegistration.Dynamic addServlet(String n, Class<? extends Servlet> c) { return null; }
    @Override public ServletRegistration.Dynamic addJspFile(String n, String f) { return null; }
    @Override public <T extends Servlet> T createServlet(Class<T> c) { return null; }
    @Override public ServletRegistration getServletRegistration(String name) { return null; }
    @Override public Map<String, ? extends ServletRegistration> getServletRegistrations() { return Map.of(); }
    @Override public FilterRegistration.Dynamic addFilter(String n, String c) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String n, Filter f) { return null; }
    @Override public FilterRegistration.Dynamic addFilter(String n, Class<? extends Filter> c) { return null; }
    @Override public <T extends Filter> T createFilter(Class<T> c) { return null; }
    @Override public FilterRegistration getFilterRegistration(String name) { return null; }
    @Override public Map<String, ? extends FilterRegistration> getFilterRegistrations() { return Map.of(); }
    @Override public SessionCookieConfig getSessionCookieConfig() { return null; }
    @Override public void setSessionTrackingModes(Set<SessionTrackingMode> modes) {}
    @Override public Set<SessionTrackingMode> getDefaultSessionTrackingModes() { return Set.of(); }
    @Override public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() { return Set.of(); }
    @Override public void addListener(String className) {}
    @Override public <T extends EventListener> void addListener(T t) {}
    @Override public void addListener(Class<? extends EventListener> c) {}
    @Override public <T extends EventListener> T createListener(Class<T> c) { return null; }
    @Override public JspConfigDescriptor getJspConfigDescriptor() { return null; }
    @Override public ClassLoader getClassLoader() { return Thread.currentThread().getContextClassLoader(); }
    @Override public void declareRoles(String... roles) {}
    @Override public String getVirtualServerName() { return "vivid"; }
    @Override public int getSessionTimeout() { return 30; }
    @Override public void setSessionTimeout(int timeout) {}
    @Override public String getRequestCharacterEncoding() { return "UTF-8"; }
    @Override public void setRequestCharacterEncoding(String enc) {}
    @Override public String getResponseCharacterEncoding() { return "UTF-8"; }
    @Override public void setResponseCharacterEncoding(String enc) {}

    public GenericApplicationContext getApplicationContext() { return applicationContext; }
}

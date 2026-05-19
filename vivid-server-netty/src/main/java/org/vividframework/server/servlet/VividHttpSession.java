package org.vividframework.server.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionBindingListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jakarta Servlet HttpSession implementation with thread-safe attribute storage.
 * @author sketch
 */
public class VividHttpSession implements HttpSession {

    private final String id;
    private final VividServletContext servletContext;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final long creationTime;
    private long lastAccessedTime;
    private int maxInactiveInterval = 1800; // 30 minutes
    private boolean isNew = true;
    private boolean invalidated;

    public VividHttpSession(String id, VividServletContext servletContext) {
        this.id = id;
        this.servletContext = servletContext;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
    }

    void access() {
        checkValid();
        this.lastAccessedTime = System.currentTimeMillis();
    }

    @Override public String getId() { return id; }
    @Override public ServletContext getServletContext() { return servletContext; }
    @Override public long getCreationTime() { return creationTime; }
    @Override public long getLastAccessedTime() { return lastAccessedTime; }
    @Override public int getMaxInactiveInterval() { return maxInactiveInterval; }
    @Override public void setMaxInactiveInterval(int interval) { this.maxInactiveInterval = interval; }
    @Override public boolean isNew() { return isNew; }

    void markNotNew() { isNew = false; }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        Object old = attributes.put(name, value);
        if (value instanceof HttpSessionBindingListener listener && old != value) {
            listener.valueBound(new HttpSessionBindingEvent(this, name, value));
        }
        if (old instanceof HttpSessionBindingListener listener) {
            listener.valueUnbound(new HttpSessionBindingEvent(this, name));
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        Object value = attributes.remove(name);
        if (value instanceof HttpSessionBindingListener listener) {
            listener.valueUnbound(new HttpSessionBindingEvent(this, name));
        }
    }

    @Override
    public void invalidate() {
        invalidated = true;
        for (String name : new HashSet<>(attributes.keySet())) {
            removeAttribute(name);
        }
        attributes.clear();
    }

    public boolean isValid() { return !invalidated && !isExpired(); }
    public boolean isExpired() {
        return System.currentTimeMillis() - lastAccessedTime > maxInactiveInterval * 1000L;
    }

    private void checkValid() {
        if (invalidated) throw new IllegalStateException("Session invalidated");
    }

}

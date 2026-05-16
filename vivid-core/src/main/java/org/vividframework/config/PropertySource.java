package org.vividframework.config;

import java.util.Map;
import java.util.Objects;

/**
 * Property source abstraction
 * @author sketch
 */
public abstract class PropertySource<T> {

    protected final String name;
    protected final T source;

    protected PropertySource(String name, T source) {
        this.name = Objects.requireNonNull(name, "Property source name must not be null");
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public T getSource() {
        return source;
    }

    public boolean containsProperty(String name) {
        return getProperty(name) != null;
    }

    public abstract Object getProperty(String name);

    public String getProperty(String name, String defaultValue) {
        Object value = getProperty(name);
        return value != null ? value.toString() : defaultValue;
    }

    public int getProperty(String name, int defaultValue) {
        Object value = getProperty(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            return Integer.parseInt(value.toString());
        }
        return defaultValue;
    }

    public long getProperty(String name, long defaultValue) {
        Object value = getProperty(name);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value != null) {
            return Long.parseLong(value.toString());
        }
        return defaultValue;
    }

    public boolean getProperty(String name, boolean defaultValue) {
        Object value = getProperty(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value != null) {
            return Boolean.parseBoolean(value.toString());
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{name='" + name + "'}";
    }

    /**
     * Map-based property source
     */
    public static class MapPropertySource extends PropertySource<Map<String, Object>> {

        public MapPropertySource(String name, Map<String, Object> source) {
            super(name, source);
        }

        @Override
        public Object getProperty(String name) {
            return getSource().get(name);
        }

        @Override
        public boolean containsProperty(String name) {
            return getSource().containsKey(name);
        }
    }

    /**
     * System properties
     */
    public static class SystemPropertySource extends PropertySource<Map<Object, Object>> {

        private static SystemPropertySource instance;

        public SystemPropertySource() {
            super("systemProperties", System.getProperties());
        }

        public static SystemPropertySource getInstance() {
            if (instance == null) {
                instance = new SystemPropertySource();
            }
            return instance;
        }

        @Override
        public Object getProperty(String name) {
            return getSource().get(name);
        }
    }
}

package org.vividframework.config;

import java.util.List;

/**
 * Environment interface for configuration
 * @author sketch
 */
public interface Environment {

    /**
     * Get property by name
     */
    String getProperty(String key);

    /**
     * Get property with default value
     */
    String getProperty(String key, String defaultValue);

    /**
     * Get property as Integer
     */
    Integer getProperty(String key, Integer defaultValue);

    /**
     * Get property as Long
     */
    Long getProperty(String key, Long defaultValue);

    /**
     * Get property as Boolean
     */
    Boolean getProperty(String key, Boolean defaultValue);

    /**
     * Get required property
     */
    String getRequiredProperty(String key);

    /**
     * Check if contains property
     */
    boolean containsProperty(String key);

    /**
     * Get active profiles
     */
    String[] getActiveProfiles();

    /**
     * Get default profiles
     */
    String[] getDefaultProfiles();

    /**
     * Check if profile is active
     */
    boolean acceptsProfiles(String... profiles);

    /**
     * Set a property
     */
    void setProperty(String key, String value);

    /**
     * Get property sources
     */
    List<PropertySource<?>> getPropertySources();

    /**
     * Simple implementation
     */
    class StandardEnvironment implements Environment {

        private final java.util.Map<String, Object> properties = new java.util.HashMap<>();
        private final java.util.List<PropertySource<?>> propertySources = new java.util.ArrayList<>();

        public StandardEnvironment() {
            propertySources.add(PropertySource.SystemPropertySource.getInstance());
        }

        @Override
        public String getProperty(String key) {
            return getProperty(key, (String) null);
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            for (PropertySource<?> source : propertySources) {
                if (source.containsProperty(key)) {
                    return source.getProperty(key, defaultValue);
                }
            }
            Object value = properties.get(key);
            return value != null ? value.toString() : defaultValue;
        }

        @Override
        public Integer getProperty(String key, Integer defaultValue) {
            String value = getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        }

        @Override
        public Long getProperty(String key, Long defaultValue) {
            String value = getProperty(key);
            return value != null ? Long.parseLong(value) : defaultValue;
        }

        @Override
        public Boolean getProperty(String key, Boolean defaultValue) {
            String value = getProperty(key);
            return value != null ? Boolean.parseBoolean(value) : defaultValue;
        }

        @Override
        public String getRequiredProperty(String key) {
            String value = getProperty(key);
            if (value == null) {
                throw new IllegalStateException("Required property '" + key + "' not found");
            }
            return value;
        }

        @Override
        public boolean containsProperty(String key) {
            for (PropertySource<?> source : propertySources) {
                if (source.containsProperty(key)) {
                    return true;
                }
            }
            return properties.containsKey(key);
        }

        @Override
        public String[] getActiveProfiles() {
            String profiles = getProperty("spring.profiles.active");
            return profiles != null ? profiles.split(",") : new String[0];
        }

        @Override
        public String[] getDefaultProfiles() {
            String profiles = getProperty("spring.profiles.default", "default");
            return profiles.split(",");
        }

        @Override
        public boolean acceptsProfiles(String... profiles) {
            String[] activeProfiles = getActiveProfiles();
            for (String profile : profiles) {
                for (String active : activeProfiles) {
                    if (active.equals(profile) || active.equals("default") && profile.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public List<PropertySource<?>> getPropertySources() {
            return java.util.Collections.unmodifiableList(propertySources);
        }

        @Override
        public void setProperty(String key, String value) {
            properties.put(key, value);
        }

        public void setProperty(String key, Object value) {
            properties.put(key, value);
        }
    }
}

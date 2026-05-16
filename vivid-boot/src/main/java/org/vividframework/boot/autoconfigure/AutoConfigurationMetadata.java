package org.vividframework.boot.autoconfigure;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Metadata reader for auto-configuration
 * @author sketch
 */
public class AutoConfigurationMetadata {

    private final Map<String, Map<String, Object>> entries;

    public AutoConfigurationMetadata(Map<String, Map<String, Object>> entries) {
        this.entries = entries;
    }

    public static AutoConfigurationMetadata load(ClassLoader classLoader) {
        Map<String, Map<String, Object>> entries = new HashMap<>();
        
        try {
            Enumeration<URL> resources = classLoader.getResources(
                "META-INF/vivid/autoconfiguration.properties");
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream is = resource.openStream()) {
                    Properties properties = new Properties();
                    properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                    
                    String className = resource.getPath();
                    // Extract class name from path
                    entries.put(className, new HashMap<>((Map) properties));
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        
        return new AutoConfigurationMetadata(entries);
    }

    public boolean hasEntry(String className) {
        return entries.containsKey(className);
    }

    public String getString(String className, String key) {
        return getString(className, key, null);
    }

    public String getString(String className, String key, String defaultValue) {
        Map<String, Object> entry = entries.get(className);
        if (entry == null) {
            return defaultValue;
        }
        Object value = entry.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public int getInt(String className, String key, int defaultValue) {
        String value = getString(className, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String className, String key, boolean defaultValue) {
        String value = getString(className, key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public Set<String> getSet(String className, String key) {
        String value = getString(className, key);
        if (value == null || value.isEmpty()) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(Arrays.asList(value.split(",")));
    }

    public Map<String, Object> getEntry(String className) {
        return entries.getOrDefault(className, Collections.emptyMap());
    }

    public Set<String> getClassNames() {
        return entries.keySet();
    }
}

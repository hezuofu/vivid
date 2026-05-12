package org.vividframework.model;

import java.util.*;

/**
 * Map-based model implementation (similar to Spring's ModelMap)
 * @author Jon Fisher
 */
public class ModelMap extends LinkedHashMap<String, Object> {

    private static final String MODEL_KEY_PREFIX = "org.vividframework.model.ModelMap";

    public ModelMap() {
        super();
    }

    public ModelMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ModelMap(Map<String, ?> initialValues) {
        super(initialValues);
    }

    /**
     * Add attribute
     */
    @Override
    public Object put(String key, Object value) {
        return super.put(key, value);
    }

    /**
     * Add attribute using default name based on type
     */
    public ModelMap addAttribute(Object attributeValue) {
        String key = getAttributeName(attributeValue);
        return (ModelMap) put(key, attributeValue);
    }

    /**
     * Add attribute with explicit name
     */
    public ModelMap addAttribute(String attributeName, Object attributeValue) {
        put(attributeName, attributeValue);
        return this;
    }

    /**
     * Add all attributes from map
     */
    public ModelMap addAllAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            putAll(attributes);
        }
        return this;
    }

    /**
     * Add all attributes, replacing existing ones
     */
    public ModelMap addAllAttributes(Collection<?> attributeValues) {
        if (attributeValues != null) {
            for (Object attributeValue : attributeValues) {
                addAttribute(attributeValue);
            }
        }
        return this;
    }

    /**
     * Merge attributes
     */
    public ModelMap mergeAttributes(Map<String, ?> attributes) {
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                if (!containsKey(key)) {
                    put(key, value);
                }
            });
        }
        return this;
    }

    /**
     * Get attribute with type check
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) get(name);
    }

    /**
     * Check if contains attribute
     */
    public boolean hasAttribute(String name) {
        return containsKey(name);
    }

    /**
     * Get all attribute names
     */
    public Set<String> getAttributeNames() {
        return keySet();
    }

    private String getAttributeName(Object attributeValue) {
        Class<?> clazz = attributeValue.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().getSimpleName() + "[]";
        }
        return clazz.getSimpleName();
    }
}

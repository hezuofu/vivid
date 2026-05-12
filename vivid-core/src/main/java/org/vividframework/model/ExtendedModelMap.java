package org.vividframework.model;

import java.util.Collection;
import java.util.Map;

/**
 * Extended ModelMap with fluent API
 * @author Jon Fisher
 */
public class ExtendedModelMap extends ModelMap {

    public ExtendedModelMap() {
        super();
    }

    public ExtendedModelMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ExtendedModelMap(ModelMap model) {
        super(model);
    }

    @Override
    public ExtendedModelMap addAttribute(Object attributeValue) {
        super.addAttribute(attributeValue);
        return this;
    }

    @Override
    public ExtendedModelMap addAttribute(String attributeName, Object attributeValue) {
        super.addAttribute(attributeName, attributeValue);
        return this;
    }

    @Override
    public ExtendedModelMap addAllAttributes(Map<String, ?> attributes) {
        super.addAllAttributes(attributes);
        return this;
    }

    @Override
    public ExtendedModelMap addAllAttributes(Collection<?> attributeValues) {
        super.addAllAttributes(attributeValues);
        return this;
    }

    @Override
    public ExtendedModelMap mergeAttributes(Map<String, ?> attributes) {
        super.mergeAttributes(attributes);
        return this;
    }

    /**
     * Fluent method for adding objects
     */
    public ExtendedModelMap and(Object attributeValue) {
        return addAttribute(attributeValue);
    }

    /**
     * Fluent method for adding objects with name
     */
    public ExtendedModelMap and(String name, Object value) {
        return addAttribute(name, value);
    }
}

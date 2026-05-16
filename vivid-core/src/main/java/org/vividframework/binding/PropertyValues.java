package org.vividframework.binding;

import java.util.Iterator;

/**
 * Property values interface
 * @author sketch
 */
public interface PropertyValues extends Iterable<PropertyValues.PropertyValue> {

    /**
     * Property value holder
     */
    class PropertyValue {
        private final String name;
        private final Object value;

        public PropertyValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public Object getPreviousValue() {
            return null;
        }

        public boolean isConverted() {
            return false;
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }
    }

    /**
     * Get all property values
     */
    PropertyValues.PropertyValue[] getPropertyValues();

    /**
     * Get property value by name
     */
    PropertyValues.PropertyValue getPropertyValue(String name);

    /**
     * Get value by name
     */
    Object get(String name);

    /**
     * Get value by name with default
     */
    Object get(String name, Object defaultValue);

    /**
     * Check if contains property
     */
    boolean contains(String name);

    /**
     * Check if empty
     */
    boolean isEmpty();

    /**
     * Get size
     */
    int size();

    /**
     * Stream iterator
     */
    Iterator<PropertyValues.PropertyValue> iterator();
}

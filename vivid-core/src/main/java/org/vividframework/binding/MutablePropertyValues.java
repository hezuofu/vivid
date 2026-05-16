package org.vividframework.binding;

import java.util.*;

/**
 * Mutable property values holder
 * @author sketch
 */
public class MutablePropertyValues implements PropertyValues {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public MutablePropertyValues() {
    }

    public MutablePropertyValues(Map<String, ?> source) {
        if (source != null) {
            putAll(source);
        }
    }

    public MutablePropertyValues(PropertyValues propertyValues) {
        if (propertyValues != null) {
            for (PropertyValues.PropertyValue pv : propertyValues) {
                add(pv.getName(), pv.getValue());
            }
        }
    }

    private void putAll(Map<String, ?> source) {
        source.forEach(values::put);
    }

    @Override
    public PropertyValues.PropertyValue[] getPropertyValues() {
        List<PropertyValues.PropertyValue> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.add(new SimplePropertyValue(entry.getKey(), entry.getValue()));
        }
        return result.toArray(new PropertyValues.PropertyValue[0]);
    }

    @Override
    public PropertyValues.PropertyValue getPropertyValue(String name) {
        Object value = values.get(name);
        return value != null ? new SimplePropertyValue(name, value) : null;
    }

    @Override
    public Object get(String name) {
        return values.get(name);
    }

    @Override
    public Object get(String name, Object defaultValue) {
        Object value = values.get(name);
        return value != null ? value : defaultValue;
    }

    @Override
    public boolean contains(String name) {
        return values.containsKey(name);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Iterator<PropertyValues.PropertyValue> iterator() {
        List<PropertyValues.PropertyValue> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.add(new SimplePropertyValue(entry.getKey(), entry.getValue()));
        }
        return result.iterator();
    }

    public MutablePropertyValues add(String name, Object value) {
        values.put(name, value);
        return this;
    }

    public MutablePropertyValues add(PropertyValues.PropertyValue propertyValue) {
        values.put(propertyValue.getName(), propertyValue.getValue());
        return this;
    }

    public MutablePropertyValues addAll(Map<String, ?> values) {
        if (values != null) {
            this.values.putAll(values);
        }
        return this;
    }

    public MutablePropertyValues addAll(PropertyValues propertyValues) {
        if (propertyValues != null) {
            for (PropertyValues.PropertyValue pv : propertyValues) {
                values.put(pv.getName(), pv.getValue());
            }
        }
        return this;
    }

    public MutablePropertyValues remove(String name) {
        values.remove(name);
        return this;
    }

    public MutablePropertyValues clear() {
        values.clear();
        return this;
    }

    public MutablePropertyValues changed() {
        return this;
    }

    public MutablePropertyValues withRawValues(Map<String, ?> rawValues) {
        if (rawValues != null) {
            for (Map.Entry<String, ?> entry : rawValues.entrySet()) {
                if (values.containsKey(entry.getKey())) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return this;
    }

    /**
     * Simple property value implementation
     */
    private static class SimplePropertyValue extends PropertyValues.PropertyValue {
        private final String name;
        private final Object value;

        SimplePropertyValue(String name, Object value) {
            super(name, value);
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getPreviousValue() {
            return value;
        }

        @Override
        public boolean isConverted() {
            return false;
        }
    }
}

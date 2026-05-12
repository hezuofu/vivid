package org.vividframework.jdbc;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Row mapper that maps rows of a ResultSet to a Java object
 * @author Jon Fisher
 */
public class BeanPropertyRowMapper<T> implements JdbcTemplate.RowMapper<T> {

    private final Class<T> mappedClass;
    private final Map<String, PropertyDescriptor> propertyDescriptorMap;
    private boolean primitivesDefaulted = false;

    public BeanPropertyRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.propertyDescriptorMap = new HashMap<>();
        initPropertyDescriptors();
    }

    private void initPropertyDescriptors() {
        try {
            PropertyDescriptor[] pds = java.beans.Introspector.getBeanInfo(mappedClass).getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (pd.getWriteMethod() != null) {
                    propertyDescriptorMap.put(pd.getName().toLowerCase(), pd);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize property descriptors", e);
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T bean = newInstance(mappedClass);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String columnLabel = rsmd.getColumnLabel(index);
            String propertyName = getPropertyName(columnLabel);
            if (propertyName == null) {
                continue;
            }

            PropertyDescriptor pd = propertyDescriptorMap.get(propertyName.toLowerCase());
            if (pd == null) {
                continue;
            }

            Object value = getColumnValue(rs, index, pd.getPropertyType());
            if (value == null && primitivesDefaulted) {
                continue;
            }

            try {
                pd.getWriteMethod().invoke(bean, value);
            } catch (Exception e) {
                // Skip this property
            }
        }

        return bean;
    }

    protected String getPropertyName(String columnLabel) {
        // Convert column names like USER_NAME to userName or user_name
        String propertyName = columnLabel;
        // Remove common prefixes/suffixes
        if (propertyName.startsWith("IS_")) {
            propertyName = propertyName.substring(3);
        }
        // Try exact match first
        if (propertyDescriptorMap.containsKey(propertyName.toLowerCase())) {
            return propertyName;
        }
        // Try camelCase conversion
        return toCamelCase(propertyName);
    }

    protected String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_' || c == '-') {
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    protected Object getColumnValue(ResultSet rs, int index, Class<?> propertyType) throws SQLException {
        Object value = rs.getObject(index);
        if (value == null) {
            return null;
        }
        return convertValue(value, propertyType);
    }

    protected Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        // Handle common type conversions
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.valueOf(value.toString());
        }
        if (targetType == long.class || targetType == Long.class) {
            return Long.valueOf(value.toString());
        }
        if (targetType == double.class || targetType == Double.class) {
            return Double.valueOf(value.toString());
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.valueOf(value.toString());
        }
        if (targetType == java.util.Date.class) {
            if (value instanceof java.sql.Timestamp) {
                return new java.util.Date(((java.sql.Timestamp) value).getTime());
            }
            if (value instanceof java.sql.Date) {
                return new java.util.Date(((java.sql.Date) value).getTime());
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    protected T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new instance of " + clazz.getName(), e);
        }
    }

    public Class<T> getMappedClass() {
        return mappedClass;
    }

    public void setPrimitivesDefaulted(boolean primitivesDefaulted) {
        this.primitivesDefaulted = primitivesDefaulted;
    }
}

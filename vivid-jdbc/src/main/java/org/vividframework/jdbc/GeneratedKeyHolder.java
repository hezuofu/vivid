package org.vividframework.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class for managing generated keys from insert operations
 * @author sketch
 */
public class GeneratedKeyHolder {

    private final List<Map<String, Object>> keys = new ArrayList<>();

    public void addGeneratedKeys(ResultSet rs) throws SQLException {
        while (rs.next()) {
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            java.util.Map<String, Object> key = new java.util.LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                key.put(columnName, value);
            }
            keys.add(key);
        }
    }

    public List<Map<String, Object>> getKeys() {
        return keys;
    }

    public Map<String, Object> getKey() {
        if (keys.isEmpty()) {
            return null;
        }
        return keys.get(0);
    }

    public Number getKeyAsNumber() {
        Map<String, Object> key = getKey();
        if (key == null || key.isEmpty()) {
            return null;
        }
        Object value = key.values().iterator().next();
        if (value instanceof Number) {
            return (Number) value;
        }
        return null;
    }

    public Long getKeyAsLong() {
        Number key = getKeyAsNumber();
        return key != null ? key.longValue() : null;
    }

    public Integer getKeyAsInt() {
        Number key = getKeyAsNumber();
        return key != null ? key.intValue() : null;
    }

    public void clear() {
        keys.clear();
    }
}

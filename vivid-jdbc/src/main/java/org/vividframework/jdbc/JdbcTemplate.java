package org.vividframework.jdbc;

import org.vividframework.beans.InitializingBean;
import org.vividframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * JDBC template for database operations
 * @author sketch
 */
public class JdbcTemplate implements InitializingBean {

    private DataSource dataSource;
    private int queryTimeout = -1;

    public JdbcTemplate() {
    }

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(dataSource, "DataSource must be set");
    }

    public <T> T execute(ConnectionCallback<T> action) throws Exception {
        Connection con = getConnection();
        try {
            return action.doInConnection(con);
        } finally {
            closeConnection(con);
        }
    }

    public <T> T query(String sql, ResultSetExtractor<T> rse) throws Exception {
        return execute(con -> {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            try {
                return rse.extractData(rs);
            } finally {
                rs.close();
                stmt.close();
            }
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws Exception {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws Exception {
        List<T> results = query(sql, rowMapper);
        if (results.isEmpty()) {
            throw new org.vividframework.jdbc.exception.EmptyResultDataAccessException(
                    "Incorrect result size: expected 1, got 0", 1);
        }
        if (results.size() > 1) {
            throw new org.vividframework.jdbc.exception.IncorrectResultSizeDataAccessException(
                    "Incorrect result size: expected 1, got " + results.size(), 1, results.size());
        }
        return results.get(0);
    }

    public <T> T queryForObject(String sql, Class<T> requiredType) throws Exception {
        return queryForObject(sql, new SingleColumnRowMapper<>(requiredType));
    }

    public Map<String, Object> queryForMap(String sql) throws Exception {
        return queryForObject(sql, new ColumnMapRowMapper());
    }

    public List<Map<String, Object>> queryForList(String sql) throws Exception {
        return query(sql, new ColumnMapRowMapper());
    }

    public int update(String sql) throws Exception {
        return execute(con -> {
            Statement stmt = con.createStatement();
            stmt.setQueryTimeout(queryTimeout);
            return stmt.executeUpdate(sql);
        });
    }

    public int update(String sql, GeneratedKeyHolder keyHolder) throws Exception {
        return execute(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setQueryTimeout(queryTimeout);
            int rows = ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys != null) {
                keyHolder.addGeneratedKeys(keys);
            }
            keys.close();
            ps.close();
            return rows;
        });
    }

    public int[] batchUpdate(String... sqls) throws Exception {
        return execute(con -> {
            Statement stmt = con.createStatement();
            stmt.setQueryTimeout(queryTimeout);
            for (String sql : sqls) {
                stmt.addBatch(sql);
            }
            return stmt.executeBatch();
        });
    }

    public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws Exception {
        return execute(con -> {
            PreparedStatement ps = psc.createPreparedStatement(con);
            try {
                return action.doInPreparedStatement(ps);
            } finally {
                ps.close();
            }
        });
    }

    public <T> T execute(String sql, PreparedStatementCallback<T> action) throws Exception {
        return execute(con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            try {
                return action.doInPreparedStatement(ps);
            } finally {
                ps.close();
            }
        });
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    protected void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    // Callback interfaces
    public interface ConnectionCallback<T> {
        T doInConnection(Connection con) throws Exception;
    }

    public interface PreparedStatementCreator {
        PreparedStatement createPreparedStatement(Connection con) throws SQLException;
    }

    public interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement ps) throws Exception;
    }

    public interface ResultSetExtractor<T> {
        T extractData(ResultSet rs) throws Exception;
    }

    public interface RowMapper<T> {
        T mapRow(ResultSet rs, int rowNum) throws Exception;
    }

    // Result set extractors
    public static class RowMapperResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
        private final RowMapper<T> rowMapper;

        public RowMapperResultSetExtractor(RowMapper<T> rowMapper) {
            this.rowMapper = rowMapper;
        }

        @Override
        public List<T> extractData(ResultSet rs) throws Exception {
            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        }
    }

    // Row mappers
    public static class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws Exception {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String key = metaData.getColumnLabel(i);
                Object value = rs.getObject(i);
                map.put(key, value);
            }
            return map;
        }
    }

    public static class SingleColumnRowMapper<T> implements RowMapper<T> {
        private final Class<T> requiredType;

        public SingleColumnRowMapper(Class<T> requiredType) {
            this.requiredType = requiredType;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws Exception {
            Object value = rs.getObject(1);
            if (value == null) {
                return null;
            }
            if (requiredType.isInstance(value)) {
                return requiredType.cast(value);
            }
            return requiredType.cast(value);
        }
    }
}

package org.vividframework.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC template with named parameter support
 * @author sketch
 */
public class NamedParameterJdbcTemplate {

    private static final Pattern NAMED_PARAMETER_PATTERN = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

    private final JdbcTemplate jdbcTemplate;

    public NamedParameterJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public NamedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public DataSource getDataSource() {
        return jdbcTemplate.getDataSource();
    }

    public <T> T execute(String sql, Map<String, ?> paramMap, JdbcTemplate.ConnectionCallback<T> action) throws Exception {
        ParsedSql parsedSql = parseSql(sql);
        return jdbcTemplate.execute(con -> {
            PreparedStatement ps = con.prepareStatement(parsedSql.sql);
            setParameters(ps, parsedSql.parameterNames, paramMap);
            try {
                return action.doInConnection(con);
            } finally {
                ps.close();
            }
        });
    }

    public <T> T query(String sql, Map<String, ?> paramMap, JdbcTemplate.ResultSetExtractor<T> rse) throws Exception {
        return execute(sql, paramMap, con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            setParameters(ps, parseSql(sql).parameterNames, paramMap);
            ResultSet rs = ps.executeQuery();
            try {
                return rse.extractData(rs);
            } finally {
                rs.close();
                ps.close();
            }
        });
    }

    public <T> List<T> query(String sql, Map<String, ?> paramMap, JdbcTemplate.RowMapper<T> rowMapper) throws Exception {
        return query(sql, paramMap, new JdbcTemplate.RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(String sql, Map<String, ?> paramMap, JdbcTemplate.RowMapper<T> rowMapper) throws Exception {
        List<T> results = query(sql, paramMap, rowMapper);
        if (results.isEmpty()) {
            throw new RuntimeException("Incorrect result size: expected 1, got 0");
        }
        if (results.size() > 1) {
            throw new RuntimeException("Incorrect result size: expected 1, got " + results.size());
        }
        return results.get(0);
    }

    public Map<String, Object> queryForMap(String sql, Map<String, ?> paramMap) throws Exception {
        return queryForObject(sql, paramMap, new JdbcTemplate.ColumnMapRowMapper());
    }

    public List<Map<String, Object>> queryForList(String sql, Map<String, ?> paramMap) throws Exception {
        return query(sql, paramMap, new JdbcTemplate.ColumnMapRowMapper());
    }

    public int update(String sql, Map<String, ?> paramMap) throws Exception {
        return execute(sql, paramMap, con -> {
            PreparedStatement ps = con.prepareStatement(sql);
            setParameters(ps, parseSql(sql).parameterNames, paramMap);
            int result = ps.executeUpdate();
            ps.close();
            return result;
        });
    }

    public int update(String sql, Map<String, ?> paramMap, GeneratedKeyHolder keyHolder) throws Exception {
        return execute(sql, paramMap, con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setParameters(ps, parseSql(sql).parameterNames, paramMap);
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

    protected ParsedSql parseSql(String sql) {
        Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        java.util.Set<String> parameterNames = new java.util.LinkedHashSet<>();
        while (matcher.find()) {
            parameterNames.add(matcher.group(1));
            matcher.appendReplacement(sb, "?");
        }
        matcher.appendTail(sb);
        return new ParsedSql(sb.toString(), parameterNames);
    }

    protected void setParameters(PreparedStatement ps, java.util.Set<String> parameterNames, Map<String, ?> paramMap) throws SQLException {
        int index = 1;
        for (String name : parameterNames) {
            Object value = paramMap.get(name);
            if (value instanceof java.util.Date) {
                ps.setTimestamp(index++, new Timestamp(((java.util.Date) value).getTime()));
            } else if (value instanceof java.sql.Date) {
                ps.setDate(index++, (java.sql.Date) value);
            } else if (value instanceof java.sql.Timestamp) {
                ps.setTimestamp(index++, (java.sql.Timestamp) value);
            } else {
                ps.setObject(index++, value);
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate.setDataSource(dataSource);
    }

    public void setQueryTimeout(int queryTimeout) {
        jdbcTemplate.setQueryTimeout(queryTimeout);
    }

    public int getQueryTimeout() {
        return jdbcTemplate.getQueryTimeout();
    }

    protected static class ParsedSql {
        final String sql;
        final java.util.Set<String> parameterNames;

        ParsedSql(String sql, java.util.Set<String> parameterNames) {
            this.sql = sql;
            this.parameterNames = parameterNames;
        }
    }
}

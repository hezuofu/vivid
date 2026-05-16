package org.vividframework.jdbc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Helper class for DataSource operations
 * @author sketch
 */
public abstract class DataSourceUtils {

    /**
     * Get a connection from the DataSource
     */
    public static Connection getConnection(DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Close the connection
     */
    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }

    /**
     * Get connection and execute action, then close the connection
     */
    public static <T> T execute(DataSource dataSource, ConnectionCallback<T> callback) throws Exception {
        Connection con = getConnection(dataSource);
        try {
            return callback.doInConnection(con);
        } finally {
            closeConnection(con);
        }
    }

    /**
     * Check if the connection is still valid
     */
    public static boolean isConnectionValid(Connection connection) {
        if (connection == null) {
            return false;
        }
        try {
            return !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Apply auto-commit setting to connection
     */
    public static void setAutoCommit(Connection connection, boolean autoCommit) throws SQLException {
        if (connection.getAutoCommit() != autoCommit) {
            connection.setAutoCommit(autoCommit);
        }
    }

    /**
     * Callback interface for connection operations
     */
    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T doInConnection(Connection connection) throws Exception;
    }
}

package org.vividframework.transaction;

import org.vividframework.beans.InitializingBean;
import org.vividframework.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DataSource transaction manager
 * @author Jon Fisher
 */
public class DataSourceTransactionManager implements PlatformTransactionManager, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceTransactionManager.class);

    private DataSource dataSource;
    private boolean enforceReadOnly = false;

    public DataSourceTransactionManager() {
    }

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize transaction manager", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(dataSource, "DataSource must be set");
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setEnforceReadOnly(boolean enforceReadOnly) {
        this.enforceReadOnly = enforceReadOnly;
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        Connection con = null;
        try {
            con = dataSource.getConnection();

            boolean existingTransaction = TransactionSynchronizationManager.hasResource(dataSource);
            int isolationLevel = definition.getIsolationLevel();
            boolean newIsolationLevel = (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT);

            if (existingTransaction) {
                // Handle existing transaction
                return suspendAndCreateNewTransaction(definition, con);
            }

            if (newIsolationLevel) {
                con.setTransactionIsolation(isolationLevel);
            }

            boolean readOnly = definition.isReadOnly();
            if (enforceReadOnly && readOnly) {
                con.setReadOnly(true);
            }

            con.setAutoCommit(false);

            return new SimpleTransactionStatus(con, readOnly, newIsolationLevel);

        } catch (SQLException e) {
            closeConnection(con);
            throw new TransactionException("Failed to get transaction", e);
        }
    }

    private TransactionStatus suspendAndCreateNewTransaction(TransactionDefinition definition, Connection con) {
        return new SimpleTransactionStatus(con, definition.isReadOnly(), false);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        SimpleTransactionStatus sts = (SimpleTransactionStatus) status;
        if (sts.isCompleted()) {
            throw new TransactionException("Transaction already completed");
        }

        Connection con = sts.getConnection();
        try {
            if (!sts.isReadOnly()) {
                con.commit();
            }
        } catch (SQLException e) {
            throw new TransactionException("Failed to commit transaction", e);
        } finally {
            closeConnection(con);
            sts.setCompleted();
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        SimpleTransactionStatus sts = (SimpleTransactionStatus) status;
        if (sts.isCompleted()) {
            throw new TransactionException("Transaction already completed");
        }

        Connection con = sts.getConnection();
        try {
            con.rollback();
        } catch (SQLException e) {
            throw new TransactionException("Failed to rollback transaction", e);
        } finally {
            closeConnection(con);
            sts.setCompleted();
            sts.setRollbackOnly();
        }
    }

    protected void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException e) {
                logger.debug("Failed to close connection", e);
            }
        }
    }

    /**
     * Simple transaction status implementation
     */
    public static class SimpleTransactionStatus implements TransactionStatus {

        private final Connection connection;
        private final boolean readOnly;
        private final boolean newIsolationLevel;
        private boolean completed = false;
        private boolean rollbackOnly = false;

        public SimpleTransactionStatus(Connection connection, boolean readOnly, boolean newIsolationLevel) {
            this.connection = connection;
            this.readOnly = readOnly;
            this.newIsolationLevel = newIsolationLevel;
        }

        public Connection getConnection() {
            return connection;
        }

        @Override
        public boolean isNewTransaction() {
            return !completed && !rollbackOnly;
        }

        @Override
        public void setRollbackOnly() {
            this.rollbackOnly = true;
        }

        @Override
        public boolean isRollbackOnly() {
            return rollbackOnly;
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted() {
            this.completed = true;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        @Override
        public boolean hasSavepoint() {
            return false;
        }

        @Override
        public Object getSavepoint() {
            return null;
        }

        @Override
        public Object createSavepoint() throws TransactionException {
            throw new UnsupportedOperationException("Savepoints not supported");
        }

        @Override
        public void rollbackToSavepoint(Object savepoint) {
            throw new UnsupportedOperationException("Savepoints not supported");
        }

        @Override
        public void releaseSavepoint(Object savepoint) {
            throw new UnsupportedOperationException("Savepoints not supported");
        }
    }

    /**
     * Transaction synchronization manager
     */
    public static class TransactionSynchronizationManager {

        private static final ThreadLocal<Map<Object, Object>> resources = ThreadLocal.withInitial(LinkedHashMap::new);

        public static boolean hasResource(Object key) {
            return resources.get().containsKey(key);
        }

        public static Object getResource(Object key) {
            return resources.get().get(key);
        }

        public static void bindResource(Object key, Object value) {
            resources.get().put(key, value);
        }

        public static Object unbindResource(Object key) {
            return resources.get().remove(key);
        }
    }
}

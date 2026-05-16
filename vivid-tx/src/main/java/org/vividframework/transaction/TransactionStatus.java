package org.vividframework.transaction;

/**
 * Transaction status
 * @author sketch
 */
public interface TransactionStatus {

    boolean isNewTransaction();

    void setRollbackOnly();

    boolean isRollbackOnly();

    boolean isCompleted();

    boolean hasSavepoint();

    Object getSavepoint();

    Object createSavepoint() throws TransactionException;

    void rollbackToSavepoint(Object savepoint);

    void releaseSavepoint(Object savepoint);
}

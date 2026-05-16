package org.vividframework.transaction;

/**
 * Platform transaction manager interface
 * @author sketch
 */
public interface PlatformTransactionManager {

    /**
     * Get transaction
     */
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

    /**
     * Commit transaction
     */
    void commit(TransactionStatus status) throws TransactionException;

    /**
     * Rollback transaction
     */
    void rollback(TransactionStatus status) throws TransactionException;
}

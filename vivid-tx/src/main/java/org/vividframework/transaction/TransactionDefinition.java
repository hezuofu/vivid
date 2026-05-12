package org.vividframework.transaction;

/**
 * Transaction definition
 * @author Jon Fisher
 */
public interface TransactionDefinition {

    int PROPAGATION_REQUIRED = 0;
    int PROPAGATION_SUPPORTS = 1;
    int PROPAGATION_MANDATORY = 2;
    int PROPAGATION_REQUIRES_NEW = 3;
    int PROPAGATION_NOT_SUPPORTED = 4;
    int PROPAGATION_NEVER = 5;
    int PROPAGATION_NESTED = 6;

    int ISOLATION_DEFAULT = -1;
    int ISOLATION_READ_UNCOMMITTED = 1;
    int ISOLATION_READ_COMMITTED = 2;
    int ISOLATION_REPEATABLE_READ = 4;
    int ISOLATION_SERIALIZABLE = 8;

    int TIMEOUT_DEFAULT = -1;

    int getPropagationBehavior();

    int getIsolationLevel();

    int getTimeout();

    boolean isReadOnly();

    String getName();

    // Default definition
    TransactionDefinition DEFAULT = new TransactionDefinition() {
        @Override
        public int getPropagationBehavior() {
            return PROPAGATION_REQUIRED;
        }

        @Override
        public int getIsolationLevel() {
            return ISOLATION_DEFAULT;
        }

        @Override
        public int getTimeout() {
            return TIMEOUT_DEFAULT;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public String getName() {
            return null;
        }
    };
}

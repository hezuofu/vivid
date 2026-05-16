package org.vividframework.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.aop.MethodInterceptor;
import org.vividframework.aop.MethodInvocation;
import org.vividframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * Transaction interceptor for declarative transaction management.
 * Wraps method execution with transaction semantics based on @Transactional annotation.
 * @author sketch
 */
public class TransactionInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionInterceptor.class);

    private PlatformTransactionManager transactionManager;

    public TransactionInterceptor() {
    }

    public TransactionInterceptor(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> targetClass = invocation.getTarget().getClass();

        // Get transaction attribute from annotation
        TransactionAttribute attr = computeTransactionAttribute(method, targetClass);
        if (attr == null) {
            // No transaction needed
            return invocation.proceed();
        }

        // Get transaction name
        String transactionName = attr.getName();
        if (transactionName == null || transactionName.isEmpty()) {
            transactionName = targetClass.getName() + "." + method.getName();
        }

        TransactionStatus status = null;

        try {
            // Get or create transaction
            TransactionDefinition definition = createTransactionDefinition(attr, transactionName);
            status = transactionManager.getTransaction(definition);

            // Execute the actual method
            Object result = invocation.proceed();

            // Commit if not marked for rollback
            if (status.isRollbackOnly()) {
                logger.debug("Transaction marked for rollback only, rolling back: {}", transactionName);
                transactionManager.rollback(status);
            } else {
                logger.debug("Committing transaction: {}", transactionName);
                transactionManager.commit(status);
            }

            return result;

        } catch (Throwable ex) {
            // Rollback on exception
            if (status != null && !status.isCompleted()) {
                try {
                    if (attr.rollbackOn(ex)) {
                        logger.debug("Rolling back transaction due to exception: {}", ex.getMessage());
                        transactionManager.rollback(status);
                    } else {
                        logger.debug("Committing transaction despite exception (not configured for rollback): {}", ex.getMessage());
                        transactionManager.commit(status);
                    }
                } catch (Throwable rbEx) {
                    logger.error("Rollback failed due to exception", rbEx);
                    throw rbEx;
                }
            }
            throw ex;
        }
    }

    protected TransactionAttribute computeTransactionAttribute(Method method, Class<?> targetClass) {
        // Check method-level annotation first
        Transactional txAttr = method.getAnnotation(Transactional.class);
        if (txAttr != null) {
            return new TransactionAttribute(txAttr);
        }

        // Check class-level annotation
        txAttr = targetClass.getAnnotation(Transactional.class);
        if (txAttr != null) {
            return new TransactionAttribute(txAttr);
        }

        return null;
    }

    protected TransactionDefinition createTransactionDefinition(TransactionAttribute attr, String transactionName) {
        return new TransactionDefinition() {
            @Override
            public int getPropagationBehavior() {
                return attr.getPropagationBehavior();
            }

            @Override
            public int getIsolationLevel() {
                return TransactionDefinition.ISOLATION_DEFAULT;
            }

            @Override
            public int getTimeout() {
                return TransactionDefinition.TIMEOUT_DEFAULT;
            }

            @Override
            public boolean isReadOnly() {
                return attr.isReadOnly();
            }

            @Override
            public String getName() {
                return transactionName;
            }
        };
    }

    /**
     * Transaction attribute wrapper for @Transactional annotation
     */
    public static class TransactionAttribute {
        private final Transactional annotation;

        public TransactionAttribute(Transactional annotation) {
            this.annotation = annotation;
        }

        public int getPropagationBehavior() {
            Class<?> propagationClass = annotation.propagation();
            if (propagationClass == TransactionDefinition.class || propagationClass == null) {
                return TransactionDefinition.PROPAGATION_REQUIRED;
            }
            
            // Map propagation class to behavior constant
            String name = propagationClass.getSimpleName();
            switch (name) {
                case "Required": return TransactionDefinition.PROPAGATION_REQUIRED;
                case "Supports": return TransactionDefinition.PROPAGATION_SUPPORTS;
                case "Mandatory": return TransactionDefinition.PROPAGATION_MANDATORY;
                case "RequiresNew": return TransactionDefinition.PROPAGATION_REQUIRES_NEW;
                case "NotSupported": return TransactionDefinition.PROPAGATION_NOT_SUPPORTED;
                case "Never": return TransactionDefinition.PROPAGATION_NEVER;
                case "Nested": return TransactionDefinition.PROPAGATION_NESTED;
                default: return TransactionDefinition.PROPAGATION_REQUIRED;
            }
        }

        public boolean isReadOnly() {
            // @Transactional doesn't have readOnly attribute in current annotation
            // This would need to be added to the annotation
            return false;
        }

        public String getName() {
            return annotation.value();
        }

        public boolean rollbackOn(Throwable ex) {
            // Check noRollbackFor
            Class<?>[] noRollbackFor = annotation.noRollbackFor();
            if (noRollbackFor != null && noRollbackFor.length > 0) {
                for (Class<?> cls : noRollbackFor) {
                    if (cls.isInstance(ex)) {
                        return false;
                    }
                }
            }

            // Default: rollback on RuntimeException and Error
            return (ex instanceof RuntimeException) || (ex instanceof Error);
        }
    }
}

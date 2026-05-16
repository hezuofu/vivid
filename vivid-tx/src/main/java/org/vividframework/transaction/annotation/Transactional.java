package org.vividframework.transaction.annotation;

import org.vividframework.transaction.TransactionDefinition;

import java.lang.annotation.*;

/**
 * Transactional annotation
 * @author sketch
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Transactional {
    String value() default "";
    Class<?> propagation() default TransactionDefinition.class;
    Class<?>[] rollbackFor() default {};
    Class<?>[] noRollbackFor() default {};
}

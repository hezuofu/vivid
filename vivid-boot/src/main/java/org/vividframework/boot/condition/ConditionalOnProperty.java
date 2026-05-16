package org.vividframework.boot.condition;

import java.lang.annotation.*;

/**
 * Condition that matches based on property value
 * @author sketch
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnProperty {

    /**
     * The property name to check
     */
    String name() default "";

    /**
     * The prefix of the property
     */
    String prefix() default "";

    /**
     * The expected value
     */
    String value() default "";

    /**
     * Whether the property must be present
     */
    boolean matchIfMissing() default false;
}

package org.vividframework.boot.condition;

import java.lang.annotation.*;

/**
 * Condition that matches when the specified bean is not present
 * @author Jon Fisher
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnMissingBean {

    /**
     * The class of the bean that must not be present
     */
    Class<?> value() default Object.class;

    /**
     * The name of the bean that must not be present
     */
    String name() default "";
}

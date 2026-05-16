package org.vividframework.boot.condition;

import java.lang.annotation.*;

/**
 * Condition that matches when the specified class is present
 * @author sketch
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConditionalOnClass {

    /**
     * The class that must be present
     */
    Class<?> value();
}

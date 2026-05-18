package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a bean should be preferred when multiple candidates
 * are eligible for autowiring.
 *
 * @author sketch
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Primary {
}

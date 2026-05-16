package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Marks a binding as singleton-scoped.
 * @author sketch
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Singleton {
}

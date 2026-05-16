package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Marks a constructor or method for dependency injection.
 * On constructors: chooses which constructor to use for injection.
 * If no @Inject is present, the no-arg constructor is used (or the
 * single constructor if only one exists).
 *
 * @author sketch
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Inject {
}

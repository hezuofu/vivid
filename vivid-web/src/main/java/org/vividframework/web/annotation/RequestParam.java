package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Request param annotation
 * @author sketch
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
    boolean required() default true;
    String defaultValue() default "";
}

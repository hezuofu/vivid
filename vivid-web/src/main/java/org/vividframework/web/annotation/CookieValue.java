package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Binds a cookie value to a controller method parameter.
 * @author sketch
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CookieValue {
    String value() default "";
    boolean required() default true;
    String defaultValue() default "";
}

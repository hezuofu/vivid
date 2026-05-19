package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Binds a session attribute to a controller method parameter.
 * @author sketch
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionAttribute {
    String value() default "";
    boolean required() default true;
}

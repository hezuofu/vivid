package org.vividframework.webmvc.annotation;

import java.lang.annotation.*;

/**
 * Annotation for mapping request header values to method parameters
 * @author sketch
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestHeader {

    /**
     * The name of the request header to bind
     */
    String value() default "";

    /**
     * Whether the header is required
     */
    boolean required() default true;

    /**
     * Default value if header is not present
     */
    String defaultValue() default "";
}

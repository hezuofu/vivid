package org.vividframework.web.annotation;

import org.vividframework.http.HttpMethod;

import java.lang.annotation.*;

/**
 * Request mapping annotation
 * @author sketch
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
    String name() default "";
    String[] path() default {};
    HttpMethod[] method() default {};
    String[] params() default {};
    String[] headers() default {};
    String[] consumes() default {};
    String[] produces() default {};
}

package org.vividframework.web.annotation;

import org.vividframework.http.HttpMethod;

import java.lang.annotation.*;

/**
 * Get mapping annotation
 * @author sketch
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = HttpMethod.GET)
public @interface GetMapping {
    String value() default "";
    String[] path() default {};
    String[] params() default {};
    String[] headers() default {};
    String[] produces() default {};
}

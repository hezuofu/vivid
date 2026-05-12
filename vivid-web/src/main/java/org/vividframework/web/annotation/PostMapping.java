package org.vividframework.web.annotation;

import org.vividframework.http.HttpMethod;

import java.lang.annotation.*;

/**
 * Post mapping annotation
 * @author Jon Fisher
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(method = HttpMethod.POST)
public @interface PostMapping {
    String value() default "";
    String[] path() default {};
    String[] params() default {};
    String[] headers() default {};
    String[] consumes() default {};
    String[] produces() default {};
}

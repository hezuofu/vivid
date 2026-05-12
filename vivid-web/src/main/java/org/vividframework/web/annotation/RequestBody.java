package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Request body annotation
 * @author Jon Fisher
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestBody {
    boolean required() default true;
}

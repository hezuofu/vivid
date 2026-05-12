package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Path variable annotation
 * @author Jon Fisher
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PathVariable {
    String value() default "";
}

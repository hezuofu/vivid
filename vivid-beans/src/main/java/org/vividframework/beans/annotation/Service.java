package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Service annotation
 * @author Jon Fisher
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    String value() default "";
}

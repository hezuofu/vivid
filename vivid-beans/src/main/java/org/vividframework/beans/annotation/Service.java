package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Service annotation
 * @author sketch
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Service {
    String value() default "";
}

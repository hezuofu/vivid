package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Bean annotation for method-level bean definition
 * @author sketch
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {
    String value() default "";
}

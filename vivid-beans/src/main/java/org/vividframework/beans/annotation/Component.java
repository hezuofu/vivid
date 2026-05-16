package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Component annotation
 * @author sketch
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default "";
}

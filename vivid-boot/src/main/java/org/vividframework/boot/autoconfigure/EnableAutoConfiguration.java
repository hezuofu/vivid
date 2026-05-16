package org.vividframework.boot.autoconfigure;

import java.lang.annotation.*;

/**
 * Enable auto configuration
 * @author sketch
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableAutoConfiguration {
    String value() default "";
}

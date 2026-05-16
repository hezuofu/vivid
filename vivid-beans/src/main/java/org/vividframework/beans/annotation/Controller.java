package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Controller annotation
 * @author sketch
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
    String value() default "";
}

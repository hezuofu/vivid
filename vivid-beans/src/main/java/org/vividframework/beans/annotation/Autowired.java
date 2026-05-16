package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Autowired annotation for dependency injection
 * @author sketch
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    boolean required() default true;
}

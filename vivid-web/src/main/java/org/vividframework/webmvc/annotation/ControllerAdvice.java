package org.vividframework.webmvc.annotation;

import java.lang.annotation.*;

/**
 * Annotation for classes that contribute to centralized exception handling
 * @author Jon Fisher
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ControllerAdvice {

    /**
     * Specify classes to which this advice applies
     */
    Class<?>[] value() default {};
}

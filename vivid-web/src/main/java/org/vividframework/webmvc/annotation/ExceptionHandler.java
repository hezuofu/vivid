package org.vividframework.webmvc.annotation;

import java.lang.annotation.*;

/**
 * Annotation for handling exceptions in controller advice
 * @author Jon Fisher
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionHandler {

    /**
     * The exception types to handle
     */
    Class<? extends Exception>[] value() default {};
}

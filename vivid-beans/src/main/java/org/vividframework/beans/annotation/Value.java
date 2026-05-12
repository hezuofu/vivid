package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Value annotation for property injection
 * @author Jon Fisher
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Value {
    String value();
}

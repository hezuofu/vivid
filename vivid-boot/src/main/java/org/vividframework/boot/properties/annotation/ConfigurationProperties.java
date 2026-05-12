package org.vividframework.boot.properties.annotation;

import java.lang.annotation.*;

/**
 * Annotation for binding external configuration to a Java bean
 * @author Jon Fisher
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationProperties {

    /**
     * The prefix of the properties to bind
     */
    String prefix() default "";

    /**
     * Whether to ignore invalid properties
     */
    boolean ignoreInvalidFields() default false;

    /**
     * Whether to ignore unknown properties
     */
    boolean ignoreUnknownFields() default true;
}

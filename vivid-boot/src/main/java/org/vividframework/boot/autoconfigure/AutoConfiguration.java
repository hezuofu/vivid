package org.vividframework.boot.autoconfigure;

import java.lang.annotation.*;

/**
 * Indicates that the class containing @Configuration can be auto-configured
 * @author Jon Fisher
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoConfiguration {
}

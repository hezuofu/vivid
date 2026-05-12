package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Response body annotation
 * @author Jon Fisher
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}

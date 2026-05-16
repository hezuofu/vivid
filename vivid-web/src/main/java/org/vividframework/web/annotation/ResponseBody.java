package org.vividframework.web.annotation;

import java.lang.annotation.*;

/**
 * Response body annotation
 * @author sketch
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}

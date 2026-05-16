package org.vividframework.web.annotation;

import org.vividframework.beans.annotation.Controller;

import java.lang.annotation.*;

/**
 * Rest controller annotation
 * @author sketch
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface RestController {
    String value() default "";
}

package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Indicates that a class declares @Bean methods and should be processed
 * by the container to generate bean definitions.
 *
 * <pre>
 * &#64;Configuration
 * public class AppConfig {
 *     &#64;Bean
 *     public Service service() { return new ServiceImpl(repo()); }
 *     &#64;Bean
 *     public Repository repo() { return new JdbcRepo(); }
 * }
 * </pre>
 *
 * @author sketch
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
    String value() default "";
}

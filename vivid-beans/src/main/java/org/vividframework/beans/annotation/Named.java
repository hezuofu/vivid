package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * String-based qualifier for dependency injection.
 *
 * <pre>
 * bind(Service.class).annotatedWith(Names.named("primary")).to(PrimaryService.class);
 * &#64;Inject public Client(@Named("primary") Service service) {}
 * </pre>
 *
 * @author sketch
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Named {
    String value();
}

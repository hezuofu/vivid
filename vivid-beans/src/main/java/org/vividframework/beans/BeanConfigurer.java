package org.vividframework.beans;

/**
 * Spring-style BeanConfigurer: programmatic bean registration.
 *
 * <pre>
 * public class AppConfig implements BeanConfigurer {
 *     public void configure(BeanDefinitionRegistry registry) {
 *         registry.registerBean(Service.class, ServiceImpl.class).scope(BeanDefinition.SCOPE_SINGLETON);
 *         registry.registerBean(Service.class).qualifier("primary").instance(new PrimaryService());
 *     }
 * }
 * </pre>
 *
 * @author sketch
 */
@FunctionalInterface
public interface BeanConfigurer {
    void configure(BeanDefinitionRegistry registry);
}

package org.vividframework.beans.annotation;

import java.lang.annotation.*;

/**
 * Indicates component scanning configurations.
 * @author sketch
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {
    
    /**
     * Base packages to scan for components
     */
    String[] value() default {};
    
    /**
     * Alias for value
     */
    String[] basePackages() default {};
    
    /**
     * Classes to use for determining base packages
     */
    Class<?>[] basePackageClasses() default {};
    
    /**
     * Include filters
     */
    ComponentScan.Filter[] includeFilters() default {};
    
    /**
     * Exclude filters
     */
    ComponentScan.Filter[] excludeFilters() default {};
    
    /**
     * Filter type
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface Filter {
        FilterType type() default FilterType.ANNOTATION;
        Class<?>[] value() default {};
        String[] pattern() default {};
    }
    
    enum FilterType {
        ANNOTATION,
        ASSIGNABLE_TYPE,
        ASPECTJ,
        REGEX,
        CUSTOM
    }
}

package org.vividframework.io;

import java.io.IOException;

/**
 * Interface for loading resources
 * @author Jon Fisher
 */
public interface ResourceLoader {

    /**
     * Return the class loader for loading resources
     */
    ClassLoader getClassLoader();

    /**
     * Return a resource at the given location
     */
    Resource getResource(String location);

    /**
     * Get resource with class loader fallback
     */
    default Resource getResource(String location, ClassLoader classLoader) {
        return new ClassPathResource(location, classLoader);
    }
}

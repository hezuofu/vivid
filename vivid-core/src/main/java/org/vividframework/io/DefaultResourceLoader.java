package org.vividframework.io;

import java.io.File;
import java.io.IOException;

/**
 * Default implementation of ResourceLoader
 * @author Jon Fisher
 */
public class DefaultResourceLoader implements ResourceLoader {

    private ClassLoader classLoader;

    public DefaultResourceLoader() {
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    public DefaultResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Resource getResource(String location) {
        if (location.startsWith("classpath:")) {
            return new ClassPathResource(location.substring(11), classLoader);
        }
        if (location.startsWith("file:")) {
            try {
                return new FileSystemResource(new File(location.substring(5)));
            } catch (Exception e) {
                return null;
            }
        }
        if (location.startsWith("http:") || location.startsWith("https:")) {
            try {
                return new UrlResource(location);
            } catch (IOException e) {
                return null;
            }
        }
        // Try as file system resource first
        File file = new File(location);
        if (file.exists()) {
            return new FileSystemResource(file);
        }
        // Fall back to classpath
        return new ClassPathResource(location, classLoader);
    }
}

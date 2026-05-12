package org.vividframework.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Resource implementation for classpath resources
 * @author Jon Fisher
 */
public class ClassPathResource implements Resource {

    private final String path;
    private final ClassLoader classLoader;
    private final Class<?> clazz;

    public ClassPathResource(String path) {
        this(path, null, null);
    }

    public ClassPathResource(String path, ClassLoader classLoader) {
        this(path, classLoader, null);
    }

    public ClassPathResource(String path, Class<?> clazz) {
        this(path, null, clazz);
    }

    public ClassPathResource(String path, ClassLoader classLoader, Class<?> clazz) {
        this.path = cleanPath(path);
        this.classLoader = classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
        this.clazz = clazz;
    }

    private String cleanPath(String path) {
        if (path == null) {
            return "";
        }
        // Remove leading/trailing slashes
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @Override
    public boolean exists() {
        return getURL() != null;
    }

    @Override
    public boolean isReadable() {
        return exists();
    }

    @Override
    public URL getURL() {
        try {
            if (clazz != null) {
                return clazz.getResource(path);
            }
            return classLoader.getResource(path);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public java.net.URI getURI() throws IOException {
        URL url = getURL();
        if (url == null) {
            throw new IOException("Resource not found: " + path);
        }
        try {
            return url.toURI();
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Failed to convert URL to URI", e);
        }
    }

    @Override
    public File getFile() throws IOException {
        URL url = getURL();
        if (url == null) {
            throw new IOException("Resource not found: " + path);
        }
        return new File(url.getFile());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is;
        if (clazz != null) {
            is = clazz.getResourceAsStream(path);
        } else {
            is = classLoader.getResourceAsStream(path);
        }
        if (is == null) {
            throw new IOException("Could not open InputStream for resource: " + path);
        }
        return is;
    }

    @Override
    public String getDescription() {
        return "class path resource [" + path + "]";
    }

    @Override
    public String getFilename() {
        int lastSlash = path.lastIndexOf('/');
        return (lastSlash != -1) ? path.substring(lastSlash + 1) : path;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String newPath = path + "/" + relativePath;
        return new ClassPathResource(newPath, classLoader);
    }

    public String getPath() {
        return path;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClassPathResource)) {
            return false;
        }
        ClassPathResource other = (ClassPathResource) obj;
        return path.equals(other.path) &&
               classLoader.equals(other.classLoader);
    }

    @Override
    public int hashCode() {
        return path.hashCode() * classLoader.hashCode();
    }
}

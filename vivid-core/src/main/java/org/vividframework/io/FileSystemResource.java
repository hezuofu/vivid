package org.vividframework.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Resource implementation for file system resources
 * @author Jon Fisher
 */
public class FileSystemResource implements Resource {

    private final String path;
    private final File file;

    public FileSystemResource(String path) {
        this.path = path;
        this.file = new File(path);
    }

    public FileSystemResource(File file) {
        this.file = file;
        this.path = file.getPath();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean isReadable() {
        return file.canRead() && !file.isDirectory();
    }

    @Override
    public URL getURL() throws IOException {
        return file.toURI().toURL();
    }

    @Override
    public URI getURI() throws IOException {
        return file.toURI();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new java.io.FileInputStream(file);
    }

    @Override
    public String getDescription() {
        return "file system resource [" + file.getAbsolutePath() + "]";
    }

    @Override
    public String getFilename() {
        return file.getName();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        String newPath = file.getParent() + File.separator + relativePath;
        return new FileSystemResource(newPath);
    }

    public String getPath() {
        return path;
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
        if (!(obj instanceof FileSystemResource)) {
            return false;
        }
        FileSystemResource other = (FileSystemResource) obj;
        return file.equals(other.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}

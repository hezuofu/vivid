package org.vividframework.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Resource implementation for InputStream resources
 * @author sketch
 */
public class InputStreamResource implements Resource {

    private final InputStream inputStream;
    private final String description;

    public InputStreamResource(InputStream inputStream) {
        this(inputStream, "InputStream resource");
    }

    public InputStreamResource(InputStream inputStream, String description) {
        this.inputStream = inputStream;
        this.description = description;
    }

    @Override
    public boolean exists() {
        return inputStream != null;
    }

    @Override
    public boolean isReadable() {
        return inputStream != null;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public URL getURL() throws IOException {
        throw new UnsupportedOperationException(
            "Cannot get URL for InputStreamResource");
    }

    @Override
    public URI getURI() throws IOException {
        throw new UnsupportedOperationException(
            "Cannot get URI for InputStreamResource");
    }

    @Override
    public File getFile() throws IOException {
        throw new UnsupportedOperationException(
            "Cannot get File for InputStreamResource");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputStream == null) {
            throw new IOException("InputStream is not available");
        }
        return inputStream;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        throw new UnsupportedOperationException(
            "Cannot create relative resource for InputStreamResource");
    }

    public InputStream getRawInputStream() {
        return inputStream;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

package org.vividframework.io;

import java.io.*;
import java.net.URI;
import java.net.URL;

/**
 * Resource implementation for byte array resources
 * @author Jon Fisher
 */
public class ByteArrayResource implements Resource {

    private final byte[] byteArray;
    private final String description;

    public ByteArrayResource(byte[] byteArray) {
        this(byteArray, "Byte array resource");
    }

    public ByteArrayResource(byte[] byteArray, String description) {
        this.byteArray = byteArray != null ? byteArray : new byte[0];
        this.description = description;
    }

    @Override
    public boolean exists() {
        return byteArray.length > 0;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public URL getURL() throws IOException {
        throw new UnsupportedOperationException(
            "Cannot get URL for ByteArrayResource");
    }

    @Override
    public URI getURI() throws IOException {
        throw new UnsupportedOperationException(
            "Cannot get URI for ByteArrayResource");
    }

    @Override
    public File getFile() throws IOException {
        throw new UnsupportedOperationException(
            "Cannot get File for ByteArrayResource");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(byteArray);
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
            "Cannot create relative resource for ByteArrayResource");
    }

    public byte[] getByteArray() {
        return byteArray;
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
        if (!(obj instanceof ByteArrayResource)) {
            return false;
        }
        ByteArrayResource other = (ByteArrayResource) obj;
        if (this.byteArray.length != other.byteArray.length) {
            return false;
        }
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] != other.byteArray[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return byteArray.hashCode();
    }
}

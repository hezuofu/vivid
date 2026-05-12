package org.vividframework.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Resource interface for accessing various input sources
 * @author Jon Fisher
 */
public interface Resource {

    /**
     * Return whether this resource actually exists in physical form
     */
    boolean exists();

    /**
     * Return whether the contents of this resource can be read
     */
    default boolean isReadable() {
        return exists();
    }

    /**
     * Return whether this resource is open
     */
    default boolean isOpen() {
        return false;
    }

    /**
     * Return a URL for this resource
     */
    URL getURL() throws IOException;

    /**
     * Return a URI for this resource
     */
    URI getURI() throws IOException;

    /**
     * Return a File reference for this resource
     */
    File getFile() throws IOException;

    /**
     * Return an InputStream for this resource
     */
    InputStream getInputStream() throws IOException;

    /**
     * Return the description for this resource
     */
    String getDescription();

    /**
     * Return the filename of this resource
     */
    String getFilename();

    /**
     * Create a relative resource from this resource
     */
    Resource createRelative(String relativePath) throws IOException;
}

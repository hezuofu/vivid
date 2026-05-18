package org.vividframework.http;

import java.io.InputStream;

/**
 * Represents an uploaded file from a multipart/form-data request.
 * @author sketch
 */
public interface MultipartFile {

    /** Original filename from the client. */
    String getOriginalFilename();

    /** MIME type declared in the upload. */
    String getContentType();

    /** File size in bytes. */
    long getSize();

    /** File content as raw bytes. */
    byte[] getBytes();

    /** File content as input stream. */
    InputStream getInputStream();

    /** Form field name. */
    String getName();

    /** Whether this is an empty file upload. */
    default boolean isEmpty() {
        return getSize() == 0;
    }
}

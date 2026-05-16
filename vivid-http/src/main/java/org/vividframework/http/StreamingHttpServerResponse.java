package org.vividframework.http;

import java.io.OutputStream;

/**
 * Extended server response supporting streaming writes.
 * Used by streaming views (file download, SSE, etc.).
 * @author Jon Fisher
 */
public interface StreamingHttpServerResponse extends HttpServerResponse {

    /**
     * Get the output stream for writing response body chunks.
     */
    OutputStream getOutputStream();

    /**
     * Flush buffered data to the client.
     */
    void flush();

    /**
     * Signal that streaming is complete.
     */
    void complete();

    /**
     * Write a chunk of data and flush immediately.
     */
    default void write(byte[] data) {
        try {
            getOutputStream().write(data);
            flush();
        } catch (Exception e) {
            throw new RuntimeException("Error writing streaming response", e);
        }
    }

    /**
     * Write a string chunk with UTF-8 encoding and flush.
     */
    default void write(String data) {
        write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}

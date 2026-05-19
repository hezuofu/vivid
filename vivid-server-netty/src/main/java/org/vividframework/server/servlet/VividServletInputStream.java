package org.vividframework.server.servlet;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ServletInputStream backed by a byte array or InputStream.
 * @author sketch
 */
public class VividServletInputStream extends ServletInputStream {
    private final InputStream delegate;

    public VividServletInputStream(InputStream delegate) { this.delegate = delegate; }

    @Override public int read() throws IOException { return delegate.read(); }
    @Override public int read(byte[] b, int off, int len) throws IOException { return delegate.read(b, off, len); }
    @Override public boolean isFinished() {
        try { return delegate.available() == 0; } catch (IOException e) { return true; }
    }
    @Override public boolean isReady() { return true; }
    @Override public void setReadListener(ReadListener listener) {}
    @Override public void close() throws IOException { delegate.close(); }
}

package org.vividframework.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ServletOutputStream backed by a growable byte array.
 * @author sketch
 */
public class VividServletOutputStream extends ServletOutputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @Override public void write(int b) throws IOException { buffer.write(b); }
    @Override public void write(byte[] b, int off, int len) throws IOException { buffer.write(b, off, len); }
    @Override public boolean isReady() { return true; }
    @Override public void setWriteListener(WriteListener listener) {}

    public byte[] toByteArray() { return buffer.toByteArray(); }
    public int size() { return buffer.size(); }
}

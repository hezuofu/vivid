package org.vividframework.server.servlet;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.vividframework.http.HttpHeaders;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Jakarta Servlet HttpServletResponse implementation backed by Vivid's HttpServletResponse.
 * @author sketch
 */
public class VividHttpServletResponse implements HttpServletResponse {

    private final VividServletOutputStream outputStream = new VividServletOutputStream();
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private int status = 200;
    private String message = "OK";
    private boolean committed;
    private String contentType = "text/html;charset=UTF-8";
    private String characterEncoding = "UTF-8";
    private int bufferSize = 8192;
    private Locale locale = Locale.getDefault();
    private final List<Cookie> cookies = new ArrayList<>();

    @Override public String getCharacterEncoding() { return characterEncoding; }
    @Override public String getContentType() { return contentType; }
    @Override public ServletOutputStream getOutputStream() { return outputStream; }
    @Override public PrintWriter getWriter() { return new PrintWriter(outputStream, true, StandardCharsets.UTF_8); }
    @Override public void setCharacterEncoding(String charset) { this.characterEncoding = charset; }
    @Override public void setContentLength(int len) { setHeader("Content-Length", String.valueOf(len)); }
    @Override public void setContentLengthLong(long len) { setHeader("Content-Length", String.valueOf(len)); }
    @Override public void setContentType(String type) {
        this.contentType = type;
        setHeader(HttpHeaders.CONTENT_TYPE, type);
    }
    @Override public void setBufferSize(int size) { this.bufferSize = size; }
    @Override public int getBufferSize() { return bufferSize; }
    @Override public void flushBuffer() throws IOException { outputStream.flush(); }
    @Override public void resetBuffer() { /* not supported */ }
    @Override public boolean isCommitted() { return committed; }
    @Override public void reset() { headers.clear(); status = 200; }
    @Override public void setLocale(Locale loc) { this.locale = loc; }
    @Override public Locale getLocale() { return locale; }

    @Override public void addCookie(Cookie cookie) { cookies.add(cookie); }
    @Override public boolean containsHeader(String name) {
        return headers.containsKey(name.toLowerCase());
    }
    @Override public String encodeURL(String url) { return url; }
    @Override public String encodeRedirectURL(String url) { return url; }
    @Override public void sendError(int sc, String msg) throws IOException {
        setStatus(sc); this.message = msg; committed = true;
    }
    @Override public void sendError(int sc) throws IOException { sendError(sc, ""); }
    @Override public void sendRedirect(String location) throws IOException {
        setStatus(302);
        setHeader(HttpHeaders.LOCATION, location);
        committed = true;
    }
    @Override public void setDateHeader(String name, long date) { setHeader(name, String.valueOf(date)); }
    @Override public void addDateHeader(String name, long date) { addHeader(name, String.valueOf(date)); }
    @Override public void setHeader(String name, String value) {
        headers.put(name.toLowerCase(), new ArrayList<>(List.of(value)));
    }
    @Override public void addHeader(String name, String value) {
        headers.computeIfAbsent(name.toLowerCase(), k -> new ArrayList<>()).add(value);
    }
    @Override public void setIntHeader(String name, int value) { setHeader(name, String.valueOf(value)); }
    @Override public void addIntHeader(String name, int value) { addHeader(name, String.valueOf(value)); }
    @Override public void setStatus(int sc) { this.status = sc; this.message = "OK"; }
    @Override public int getStatus() { return status; }
    @Override public String getHeader(String name) {
        List<String> vals = headers.get(name.toLowerCase());
        return vals != null && !vals.isEmpty() ? vals.get(0) : null;
    }
    @Override public Collection<String> getHeaders(String name) {
        return headers.getOrDefault(name.toLowerCase(), List.of());
    }
    @Override public Collection<String> getHeaderNames() { return headers.keySet(); }

    // --- Vivid-specific ---

    /** Get the written body as bytes. */
    public byte[] getBody() { return outputStream.toByteArray(); }

    /** Mark as committed. */
    public void setCommitted(boolean c) { this.committed = c; }

    /** Get all cookies. */
    public List<Cookie> getCookies() { return cookies; }

    /** Convert to Vivid immutable response. */
    public org.vividframework.http.HttpServletResponse toVividResponse() {
        return org.vividframework.http.HttpServletResponse.builder()
                .status(status)
                .headers(vividHeaders())
                .content(outputStream.toByteArray())
                .build();
    }

    private org.vividframework.http.HttpHeaders vividHeaders() {
        org.vividframework.http.HttpHeaders h = new org.vividframework.http.HttpHeaders();
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            for (String v : e.getValue()) h.add(e.getKey(), v);
        }
        return h;
    }
}

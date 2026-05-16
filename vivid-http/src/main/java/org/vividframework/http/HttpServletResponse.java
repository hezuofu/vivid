package org.vividframework.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Immutable HTTP Response representation
 * @author sketch
 */
public final class HttpServletResponse {

    private final int status;
    private final HttpStatus httpStatus;
    private final String statusMessage;
    private final HttpHeaders headers;
    private final byte[] content;
    private final Charset charset;

    private HttpServletResponse(Builder builder) {
        this.status = builder.status;
        this.httpStatus = builder.httpStatus != null ? builder.httpStatus : HttpStatus.resolve(builder.status);
        this.statusMessage = builder.statusMessage;
        this.headers = builder.headers;
        this.content = builder.content;
        this.charset = builder.charset != null ? builder.charset : StandardCharsets.UTF_8;
    }

    public int getStatus() {
        return status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public byte[] getContent() {
        return content;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getContentAsString() {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content, charset);
    }

    public long getContentLength() {
        return content != null ? content.length : 0;
    }

    public MediaType getContentType() {
        return headers.getContentType();
    }

    public String getContentTypeValue() {
        return headers.getFirst(HttpHeaders.CONTENT_TYPE);
    }

    public boolean isCommitted() {
        // Response is committed when status and headers have been sent
        return status > 0 || !headers.isEmpty();
    }

    // ========== Mutable methods for Views ==========

    /**
     * Set content type
     */
    public HttpServletResponse setContentType(String contentType) {
        return mutate().contentType(contentType).build();
    }

    /**
     * Set content type with MediaType
     */
    public HttpServletResponse setContentType(MediaType contentType) {
        return mutate().contentType(contentType).build();
    }

    /**
     * Set response content
     */
    public HttpServletResponse setContent(byte[] content) {
        return mutate().content(content).build();
    }

    /**
     * Set response content as string
     */
    public HttpServletResponse setContent(String content) {
        return mutate().content(content).build();
    }

    /**
     * Set status code
     */
    public HttpServletResponse setStatus(int status) {
        return mutate().status(status).build();
    }

    /**
     * Set status
     */
    public HttpServletResponse setStatus(HttpStatus status) {
        return mutate().status(status).build();
    }

    /**
     * Set header
     */
    public HttpServletResponse setHeader(String name, String value) {
        return mutate().header(name, value).build();
    }

    /**
     * Set JSON content
     */
    public HttpServletResponse setJson(String json) {
        return mutate().json(json).build();
    }

    /**
     * Set HTML content
     */
    public HttpServletResponse setHtml(String html) {
        return mutate().html(html).build();
    }

    /**
     * Set text content
     */
    public HttpServletResponse setText(String text) {
        return mutate().text(text).build();
    }

    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HttpServletResponse of(int status) {
        return builder().status(status).build();
    }

    public static HttpServletResponse of(HttpStatus status) {
        return builder().status(status).build();
    }

    public static HttpServletResponse ok() {
        return builder().status(HttpStatus.OK).build();
    }

    public static HttpServletResponse created() {
        return builder().status(HttpStatus.CREATED).build();
    }

    public static HttpServletResponse noContent() {
        return builder().status(HttpStatus.NO_CONTENT).build();
    }

    public static HttpServletResponse badRequest() {
        return builder().status(HttpStatus.BAD_REQUEST).build();
    }

    public static HttpServletResponse notFound() {
        return builder().status(HttpStatus.NOT_FOUND).build();
    }

    public static HttpServletResponse internalServerError() {
        return builder().status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    public static class Builder {
        private int status = 200;
        private HttpStatus httpStatus;
        private String statusMessage;
        private HttpHeaders headers = new HttpHeaders();
        private byte[] content;
        private Charset charset;

        public Builder() {}

        public Builder(HttpServletResponse response) {
            this.status = response.status;
            this.httpStatus = response.httpStatus;
            this.statusMessage = response.statusMessage;
            this.headers = response.headers;
            this.content = response.content;
            this.charset = response.charset;
        }

        public Builder status(int status) {
            this.status = status;
            this.httpStatus = null;
            return this;
        }

        public Builder status(HttpStatus status) {
            this.httpStatus = status;
            this.status = status.getCode();
            this.statusMessage = status.getReason();
            return this;
        }

        public Builder statusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        public Builder headers(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public Builder content(byte[] content) {
            this.content = content;
            if (content != null && !this.headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                this.headers.setContentLength(content.length);
            }
            return this;
        }

        public Builder content(String content) {
            return content(content, StandardCharsets.UTF_8);
        }

        public Builder content(String content, Charset charset) {
            this.charset = charset;
            this.content = content != null ? content.getBytes(charset) : null;
            return this;
        }

        public Builder contentType(MediaType contentType) {
            this.headers.setContentType(contentType);
            return this;
        }

        public Builder contentType(String contentType) {
            this.headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            return this;
        }

        public Builder charset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder cookie(HttpCookie cookie) {
            this.headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
            return this;
        }

        public Builder location(String location) {
            this.headers.set(HttpHeaders.LOCATION, location);
            return this;
        }

        public Builder cacheControl(String cacheControl) {
            this.headers.set(HttpHeaders.CACHE_CONTROL, cacheControl);
            return this;
        }

        public Builder lastModified(long lastModified) {
            this.headers.setLastModified(lastModified);
            return this;
        }

        public Builder body(byte[] content) {
            return content(content);
        }

        public Builder body(String content) {
            return content(content);
        }

        public Builder json(String json) {
            return content(json, StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON);
        }

        public Builder html(String html) {
            return content(html, StandardCharsets.UTF_8)
                    .contentType(MediaType.TEXT_HTML);
        }

        public Builder text(String text) {
            return content(text, StandardCharsets.UTF_8)
                    .contentType(MediaType.TEXT_PLAIN);
        }

        public Builder ok() {
            return status(HttpStatus.OK);
        }

        public Builder created() {
            return status(HttpStatus.CREATED);
        }

        public Builder noContent() {
            return status(HttpStatus.NO_CONTENT);
        }

        public Builder badRequest() {
            return status(HttpStatus.BAD_REQUEST);
        }

        public Builder notFound() {
            return status(HttpStatus.NOT_FOUND);
        }

        public Builder unauthorized() {
            return status(HttpStatus.UNAUTHORIZED);
        }

        public Builder forbidden() {
            return status(HttpStatus.FORBIDDEN);
        }

        public Builder serverError() {
            return status(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        public Builder redirect(String url) {
            return status(HttpStatus.FOUND)
                    .location(url);
        }

        public Builder permanentRedirect(String url) {
            return status(HttpStatus.MOVED_PERMANENTLY)
                    .location(url);
        }

        public Builder temporaryRedirect(String url) {
            return status(HttpStatus.TEMPORARY_REDIRECT)
                    .location(url);
        }

        public HttpServletResponse build() {
            return new HttpServletResponse(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpServletResponse{");
        sb.append("status=").append(status);
        if (statusMessage != null) {
            sb.append(" ").append(statusMessage);
        }
        sb.append(", contentLength=").append(getContentLength());
        sb.append(", contentType=").append(getContentTypeValue());
        sb.append("}");
        return sb.toString();
    }
}

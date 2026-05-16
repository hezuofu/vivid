package org.vividframework.http;

import org.vividframework.http.*;

import java.nio.charset.Charset;
import java.util.function.Supplier;

/**
 * Server-side HTTP response interface
 * @author sketch
 */
public interface HttpServerResponse {

    /**
     * Get the HTTP status code
     */
    int getStatus();

    /**
     * Get HTTP status
     */
    HttpStatus getHttpStatus();

    /**
     * Get status message
     */
    String getStatusMessage();

    /**
     * Get response headers
     */
    HttpHeaders getHeaders();

    /**
     * Get response body
     */
    byte[] getBody();

    /**
     * Set status code
     */
    HttpServerResponse status(int status);

    /**
     * Set HTTP status
     */
    HttpServerResponse status(HttpStatus status);

    /**
     * Set header
     */
    HttpServerResponse header(String name, String value);

    /**
     * Add header
     */
    HttpServerResponse addHeader(String name, String value);

    /**
     * Set content type
     */
    HttpServerResponse contentType(MediaType contentType);

    /**
     * Set content type from string
     */
    HttpServerResponse contentType(String contentType);

    /**
     * Set charset
     */
    HttpServerResponse charset(Charset charset);

    /**
     * Set response body
     */
    HttpServerResponse body(byte[] content);

    /**
     * Set string body
     */
    HttpServerResponse body(String content);

    /**
     * Set string body with charset
     */
    HttpServerResponse body(String content, Charset charset);

    /**
     * Set JSON body
     */
    HttpServerResponse json(String json);

    /**
     * Set HTML body
     */
    HttpServerResponse html(String html);

    /**
     * Set plain text body
     */
    HttpServerResponse text(String text);

    /**
     * Set cookie
     */
    HttpServerResponse cookie(HttpCookie cookie);

    /**
     * Set location header (for redirects)
     */
    HttpServerResponse location(String location);

    /**
     * Set cache control header
     */
    HttpServerResponse cacheControl(String cacheControl);

    /**
     * Set last modified timestamp
     */
    HttpServerResponse lastModified(long lastModified);

    /**
     * Send redirect
     */
    HttpServerResponse redirect(String url);

    /**
     * Convenience: set 200 OK
     */
    default HttpServerResponse ok() {
        return status(HttpStatus.OK);
    }

    /**
     * Convenience: set 201 Created
     */
    default HttpServerResponse created() {
        return status(HttpStatus.CREATED);
    }

    /**
     * Convenience: set 204 No Content
     */
    default HttpServerResponse noContent() {
        return status(HttpStatus.NO_CONTENT);
    }

    /**
     * Convenience: set 400 Bad Request
     */
    default HttpServerResponse badRequest() {
        return status(HttpStatus.BAD_REQUEST);
    }

    /**
     * Convenience: set 404 Not Found
     */
    default HttpServerResponse notFound() {
        return status(HttpStatus.NOT_FOUND);
    }

    /**
     * Convenience: set 500 Internal Server Error
     */
    default HttpServerResponse serverError() {
        return status(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Check if response is committed (headers sent)
     */
    boolean isCommitted();

    /**
     * Check if response has a body
     */
    default boolean hasBody() {
        byte[] body = getBody();
        return body != null && body.length > 0;
    }

    /**
     * Get content length
     */
    default long getContentLength() {
        byte[] body = getBody();
        return body != null ? body.length : 0;
    }

    /**
     * Get content type
     */
    default MediaType getContentType() {
        return getHeaders().getContentType();
    }

    /**
     * Get content as string
     */
    default String getBodyAsString() {
        byte[] body = getBody();
        if (body == null || body.length == 0) {
            return "";
        }
        String charsetStr = getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        Charset charset = Charset.forName("UTF-8");
        if (charsetStr != null && charsetStr.contains("charset=")) {
            try {
                charset = Charset.forName(charsetStr.split("charset=")[1].split("[; ]")[0]);
            } catch (Exception ignored) {
            }
        }
        return new String(body, charset);
    }

    /**
     * Convert to immutable response
     */
    HttpServletResponse toImmutable();

    /**
     * Create response from supplier
     */
    static HttpServerResponse from(Supplier<HttpServletResponse> supplier) {
        HttpServletResponse response = supplier.get();
        return new HttpServerResponse() {
            private HttpServletResponse delegate = response;

            @Override
            public int getStatus() {
                return delegate.getStatus();
            }

            @Override
            public HttpStatus getHttpStatus() {
                return delegate.getHttpStatus();
            }

            @Override
            public String getStatusMessage() {
                return delegate.getStatusMessage();
            }

            @Override
            public HttpHeaders getHeaders() {
                return delegate.getHeaders();
            }

            @Override
            public byte[] getBody() {
                return delegate.getContent();
            }

            @Override
            public HttpServerResponse status(int status) {
                delegate = delegate.mutate().status(status).build();
                return this;
            }

            @Override
            public HttpServerResponse status(HttpStatus status) {
                delegate = delegate.mutate().status(status).build();
                return this;
            }

            @Override
            public HttpServerResponse header(String name, String value) {
                delegate = delegate.mutate().header(name, value).build();
                return this;
            }

            @Override
            public HttpServerResponse addHeader(String name, String value) {
                HttpHeaders newHeaders = new HttpHeaders();
                for (String key : delegate.getHeaders().keySet()) {
                    for (String val : delegate.getHeaders().get(key)) {
                        newHeaders.add(key, val);
                    }
                }
                newHeaders.add(name, value);
                delegate = delegate.mutate().headers(newHeaders).build();
                return this;
            }

            @Override
            public HttpServerResponse contentType(MediaType contentType) {
                delegate = delegate.mutate().contentType(contentType).build();
                return this;
            }

            @Override
            public HttpServerResponse contentType(String contentType) {
                delegate = delegate.mutate().contentType(contentType).build();
                return this;
            }

            @Override
            public HttpServerResponse charset(Charset charset) {
                delegate = delegate.mutate().charset(charset).build();
                return this;
            }

            @Override
            public HttpServerResponse body(byte[] content) {
                delegate = delegate.mutate().content(content).build();
                return this;
            }

            @Override
            public HttpServerResponse body(String content) {
                delegate = delegate.mutate().content(content).build();
                return this;
            }

            @Override
            public HttpServerResponse body(String content, Charset charset) {
                delegate = delegate.mutate().content(content, charset).build();
                return this;
            }

            @Override
            public HttpServerResponse json(String json) {
                delegate = delegate.mutate().json(json).build();
                return this;
            }

            @Override
            public HttpServerResponse html(String html) {
                delegate = delegate.mutate().html(html).build();
                return this;
            }

            @Override
            public HttpServerResponse text(String text) {
                delegate = delegate.mutate().text(text).build();
                return this;
            }

            @Override
            public HttpServerResponse cookie(HttpCookie cookie) {
                delegate = delegate.mutate().cookie(cookie).build();
                return this;
            }

            @Override
            public HttpServerResponse location(String location) {
                delegate = delegate.mutate().location(location).build();
                return this;
            }

            @Override
            public HttpServerResponse cacheControl(String cacheControl) {
                delegate = delegate.mutate().cacheControl(cacheControl).build();
                return this;
            }

            @Override
            public HttpServerResponse lastModified(long lastModified) {
                delegate = delegate.mutate().lastModified(lastModified).build();
                return this;
            }

            @Override
            public HttpServerResponse redirect(String url) {
                delegate = delegate.mutate().redirect(url).build();
                return this;
            }

            @Override
            public boolean isCommitted() {
                return delegate.isCommitted();
            }

            @Override
            public HttpServletResponse toImmutable() {
                return delegate;
            }
        };
    }
}

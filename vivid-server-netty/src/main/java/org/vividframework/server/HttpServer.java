package org.vividframework.server;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

/**
 * HTTP Server abstraction interface
 * @author Jon Fisher
 */
public interface HttpServer {

    /**
     * Start the server
     */
    void start() throws Exception;

    /**
     * Stop the server
     */
    void stop();

    /**
     * Check if server is running
     */
    boolean isRunning();

    /**
     * Get the port the server is listening on
     */
    int getPort();

    /**
     * Get the server host address
     */
    String getHost();

    /**
     * Check if server is using SSL
     */
    boolean isSecure();

    /**
     * Get server context path
     */
    String getContextPath();

    /**
     * Register a handler for requests
     */
    HttpServer setHandler(HttpRequestHandler handler);

    /**
     * Set connection timeout
     */
    HttpServer setConnectionTimeout(long timeout, TimeUnit unit);

    /**
     * Set maximum content length
     */
    HttpServer setMaxContentLength(long maxLength);

    /**
     * Shutdown hook
     */
    default void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "vivid-shutdown"));
    }

    /**
     * HTTP Request Handler interface.
     */
    @FunctionalInterface
    interface HttpRequestHandler {
        /**
         * Handle an HTTP request.
         * @param request The request context
         * @return The response
         */
        HttpServletResponse handle(HttpServerRequest request) throws Exception;
    }

    /**
     * Streaming-aware request handler.
     * Implemented by handlers that can write streaming responses directly.
     */
    interface StreamingHttpRequestHandler extends HttpRequestHandler {
        /**
         * Handle a request with streaming support.
         * Default implementation delegates to buffered handler.
         */
        default void handle(HttpServerRequest request, org.vividframework.http.StreamingHttpServerResponse response)
                throws Exception {
            HttpServletResponse httpResponse = handle(request);
            response.status(httpResponse.getStatus());
            response.getHeaders().addAll(httpResponse.getHeaders());
            byte[] content = httpResponse.getContent();
            if (content != null && content.length > 0) {
                response.getOutputStream().write(content);
            }
            response.complete();
        }
    }

    /**
     * Async HTTP Request Handler interface.
     */
    @FunctionalInterface
    interface AsyncHttpRequestHandler {
        /**
         * Handle an HTTP request asynchronously.
         * @param request The request context
         * @param callback The callback to invoke with the response
         */
        void handle(HttpServerRequest request, HandlerCallback callback);
    }

    /**
     * Callback interface for async handlers
     */
    interface HandlerCallback {
        /**
         * Invoke with the response
         * @param response The response
         */
        void success(HttpServletResponse response);

        /**
         * Invoke with an error
         * @param error The error
         */
        default void failure(Throwable error) {
            failure(HttpServletResponse.internalServerError().mutate().content(error.getMessage()).build());
        }

        /**
         * Invoke with an error response
         * @param response The error response
         */
        void failure(HttpServletResponse response);
    }
}

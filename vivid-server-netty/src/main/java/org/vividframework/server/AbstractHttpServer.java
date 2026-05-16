package org.vividframework.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

/**
 * Abstract base class for HTTP server implementations
 * @author Jon Fisher
 */
public abstract class AbstractHttpServer implements HttpServer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String host = "0.0.0.0";
    protected int port = 8080;
    protected String contextPath = "";
    protected boolean running = false;
    protected long connectionTimeout = 30;
    protected TimeUnit connectionTimeoutUnit = TimeUnit.SECONDS;
    protected long maxContentLength = 10 * 1024 * 1024; // 10MB default

    protected HttpRequestHandler handler;

    @Override
    public void start() throws Exception {
        if (running) {
            logger.warn("Server is already running on {}:{}", host, port);
            return;
        }
        doStart();
        running = true;
        logger.info("Server started on {}:{}{}", host, port, contextPath);
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        try {
            doStop();
            running = false;
            logger.info("Server stopped");
        } catch (Exception e) {
            logger.error("Error stopping server", e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public HttpServer setHandler(HttpRequestHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public HttpServer setConnectionTimeout(long timeout, TimeUnit unit) {
        this.connectionTimeout = timeout;
        this.connectionTimeoutUnit = unit;
        return this;
    }

    @Override
    public HttpServer setMaxContentLength(long maxLength) {
        this.maxContentLength = maxLength;
        return this;
    }

    public AbstractHttpServer host(String host) {
        this.host = host;
        return this;
    }

    public AbstractHttpServer port(int port) {
        this.port = port;
        return this;
    }

    public AbstractHttpServer contextPath(String contextPath) {
        this.contextPath = contextPath != null ? contextPath : "";
        return this;
    }

    protected String getHostAddress() {
        return host;
    }

    protected int getServerPort() {
        return port;
    }

    protected String getServerContextPath() {
        return contextPath;
    }

    protected long getConnectionTimeoutMillis() {
        return connectionTimeoutUnit.toMillis(connectionTimeout);
    }

    protected long getMaxContentLength() {
        return maxContentLength;
    }

    protected HttpRequestHandler getHandler() {
        return handler;
    }

    protected void checkNotRunning() {
        if (running) {
            throw new IllegalStateException("Cannot modify server configuration while running");
        }
    }

    protected HttpServletResponse handleRequest(HttpServerRequest request) {
        if (handler == null) {
            return HttpServletResponse.internalServerError()
                    .mutate().content("No handler configured").build();
        }
        try {
            return handler.handle(request);
        } catch (Exception e) {
            logger.error("Error handling request: {}", request.getPath(), e);
            return HttpServletResponse.internalServerError()
                    .mutate().content("Internal Server Error: " + e.getMessage()).build();
        }
    }

    /**
     * Handle a request with streaming support.
     */
    protected void handleStreamingRequest(HttpServerRequest request,
                                           org.vividframework.http.StreamingHttpServerResponse response) {
        if (handler instanceof org.vividframework.http.HttpRequestStreamingHandler streamingHandler) {
            try {
                streamingHandler.handle(request, response);
            } catch (Exception e) {
                logger.error("Error handling streaming request: {}", request.getPath(), e);
                try {
                    response.status(500).body("Internal Server Error: " + e.getMessage());
                    response.complete();
                } catch (Exception ignored) {}
            }
        } else {
            // Fall back to buffered
            HttpServletResponse httpResponse = handleRequest(request);
            try {
                response.status(httpResponse.getStatus());
                response.getHeaders().addAll(httpResponse.getHeaders());
                byte[] body = httpResponse.getContent();
                if (body != null && body.length > 0) {
                    response.getOutputStream().write(body);
                }
                response.complete();
            } catch (Exception e) {
                logger.error("Error writing response", e);
            }
        }
    }

    /**
     * Template method for starting the server
     */
    protected abstract void doStart() throws Exception;

    /**
     * Template method for stopping the server
     */
    protected abstract void doStop() throws Exception;

    /**
     * Create and configure a builder
     */
    public static abstract class Builder<B extends Builder<B>> {
        protected String host = "0.0.0.0";
        protected int port = 8080;
        protected String contextPath = "";
        protected HttpRequestHandler handler;

        @SuppressWarnings("unchecked")
        protected B self() {
            return (B) this;
        }

        public B host(String host) {
            this.host = host;
            return self();
        }

        public B port(int port) {
            this.port = port;
            return self();
        }

        public B contextPath(String contextPath) {
            this.contextPath = contextPath;
            return self();
        }

        public B handler(HttpRequestHandler handler) {
            this.handler = handler;
            return self();
        }

        public abstract AbstractHttpServer build();
    }
}

package org.vividframework.http;

/**
 * A request handler that supports streaming responses via StreamingHttpServerResponse.
 * Implement this instead of the basic HttpRequestHandler to enable SSE, file downloads,
 * and other streaming use cases.
 * @author sketch
 */
public interface HttpRequestStreamingHandler {

    /**
     * Handle a request with streaming support.
     * The handler writes directly to the streaming response.
     */
    void handle(HttpServerRequest request, StreamingHttpServerResponse response) throws Exception;
}

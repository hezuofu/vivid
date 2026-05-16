package org.vividframework.web.event;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

import java.time.Instant;

/**
 * Base class for all web-layer events in the request lifecycle.
 * @author sketch
 */
public abstract class WebEvent {

    private final String requestId;
    private final String path;
    private final String method;
    private final Instant timestamp;
    private final long elapsedMillis;

    protected WebEvent(HttpServerRequest request, long elapsedMillis) {
        this.requestId = request.getId();
        this.path = request.getPath();
        this.method = request.getMethod().name();
        this.timestamp = Instant.now();
        this.elapsedMillis = elapsedMillis;
    }

    public String getRequestId() { return requestId; }
    public String getPath() { return path; }
    public String getMethod() { return method; }
    public Instant getTimestamp() { return timestamp; }
    public long getElapsedMillis() { return elapsedMillis; }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + method + " " + path + " @" + elapsedMillis + "ms]";
    }
}

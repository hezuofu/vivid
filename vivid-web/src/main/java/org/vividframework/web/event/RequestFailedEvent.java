package org.vividframework.web.event;

import org.vividframework.http.HttpServerRequest;

/**
 * Published when request handling fails with an exception.
 * @author sketch
 */
public class RequestFailedEvent extends WebEvent {

    private final Exception exception;

    public RequestFailedEvent(HttpServerRequest request, Exception exception, long elapsedMillis) {
        super(request, elapsedMillis);
        this.exception = exception;
    }

    public Exception getException() { return exception; }
}

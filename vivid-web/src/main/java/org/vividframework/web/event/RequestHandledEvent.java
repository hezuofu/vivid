package org.vividframework.web.event;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

/**
 * Published when a request has been successfully handled.
 * @author sketch
 */
public class RequestHandledEvent extends WebEvent {

    private final int statusCode;

    public RequestHandledEvent(HttpServerRequest request, HttpServletResponse response, long elapsedMillis) {
        super(request, elapsedMillis);
        this.statusCode = response != null ? response.getStatus() : 200;
    }

    public int getStatusCode() { return statusCode; }
}

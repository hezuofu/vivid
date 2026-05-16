package org.vividframework.web.event;

import org.vividframework.http.HttpServerRequest;

/**
 * Published when a request is received by the DispatcherHandler.
 * @author sketch
 */
public class RequestReceivedEvent extends WebEvent {

    public RequestReceivedEvent(HttpServerRequest request) {
        super(request, 0);
    }
}

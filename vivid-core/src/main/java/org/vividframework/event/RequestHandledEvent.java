package org.vividframework.event;

/**
 * Event published after request is handled
 * @author Jon Fisher
 */
public class RequestHandledEvent extends ApplicationEvent {

    private final String requestUri;
    private final String method;
    private final long processingTimeMillis;
    private final Object handler;
    private final String statusCode;
    private final Throwable exception;

    public RequestHandledEvent(Object source, String requestUri, String method,
                               long processingTimeMillis, Object handler,
                               String statusCode, Throwable exception) {
        super(source);
        this.requestUri = requestUri;
        this.method = method;
        this.processingTimeMillis = processingTimeMillis;
        this.handler = handler;
        this.statusCode = statusCode;
        this.exception = exception;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getMethod() {
        return method;
    }

    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }

    public Object getHandler() {
        return handler;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean hasException() {
        return exception != null;
    }

    @Override
    public String toString() {
        return "RequestHandledEvent{" +
                "requestUri='" + requestUri + '\'' +
                ", method='" + method + '\'' +
                ", processingTimeMillis=" + processingTimeMillis +
                ", handler=" + handler +
                ", statusCode='" + statusCode + '\'' +
                '}';
    }
}

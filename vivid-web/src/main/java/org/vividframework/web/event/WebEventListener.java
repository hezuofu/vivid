package org.vividframework.web.event;

/**
 * Listener for web lifecycle events.
 * Implement and register as a bean to receive notifications during request processing.
 *
 * <pre>
 * &#64;Component
 * public class MetricsListener implements WebEventListener {
 *     public void onRequestReceived(RequestReceivedEvent event) {
 *         metrics.increment("requests.active");
 *     }
 *     public void onRequestHandled(RequestHandledEvent event) {
 *         metrics.record("requests.duration", event.getElapsedMillis());
 *     }
 *     public void onRequestFailed(RequestFailedEvent event) {
 *         metrics.increment("requests.errors");
 *     }
 * }
 * </pre>
 *
 * @author sketch
 */
public interface WebEventListener {

    /**
     * Called when a request is received.
     */
    default void onRequestReceived(RequestReceivedEvent event) {}

    /**
     * Called when a request is handled successfully.
     */
    default void onRequestHandled(RequestHandledEvent event) {}

    /**
     * Called when request handling fails.
     */
    default void onRequestFailed(RequestFailedEvent event) {}

    /**
     * Priority: lower values execute first. Default 0.
     */
    default int getOrder() {
        return 0;
    }
}

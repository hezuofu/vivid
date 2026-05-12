package org.vividframework.event;

import java.util.EventListener;

/**
 * Application event listener interface
 * @author Jon Fisher
 */
@FunctionalInterface
public interface ApplicationListener extends EventListener {

    /**
     * Handle application event
     */
    void onApplicationEvent(ApplicationEvent event);

    /**
     * Check if this listener supports the event type
     */
    default boolean supportsEvent(Class<?> eventType) {
        return true;
    }

    /**
     * Check if this listener supports the source type
     */
    default boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    /**
     * Get order in invocation order
     */
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    interface Ordered {
        int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
        int LOWEST_PRECEDENCE = Integer.MAX_VALUE;
    }
}

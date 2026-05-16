package org.vividframework.event;

import java.util.EventObject;

/**
 * Base class for application events
 * @author sketch
 */
public abstract class ApplicationEvent extends EventObject {

    private final long timestamp;

    protected ApplicationEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    protected ApplicationEvent(Object source, long timestamp) {
        super(source);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{timestamp=" + timestamp + ", source=" + source + "}";
    }
}

package org.vividframework.event;

/**
 * Application event with payload
 * @author Jon Fisher
 */
public class PayloadApplicationEvent extends ApplicationEvent {

    private final Object payload;

    public PayloadApplicationEvent(Object source, Object payload) {
        super(source);
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "PayloadApplicationEvent{" +
                "payload=" + payload +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}

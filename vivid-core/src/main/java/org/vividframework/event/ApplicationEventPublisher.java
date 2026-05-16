package org.vividframework.event;

import java.util.function.Consumer;

/**
 * Application event publisher interface
 * @author sketch
 */
public interface ApplicationEventPublisher {

    /**
     * Publish an event
     */
    void publishEvent(ApplicationEvent event);

    /**
     * Publish an event object
     */
    void publishEvent(Object event);

    /**
     * Simple implementation that broadcasts to registered listeners
     */
    class SimpleApplicationEventPublisher implements ApplicationEventPublisher {

        private java.util.List<ApplicationListener> listeners = new java.util.ArrayList<>();
        private boolean autoRegisterListeners = true;

        @Override
        public void publishEvent(ApplicationEvent event) {
            publish(event);
        }

        @Override
        public void publishEvent(Object event) {
            publish(event instanceof ApplicationEvent ? (ApplicationEvent) event :
                    new PayloadApplicationEvent(this, event));
        }

        protected void publish(ApplicationEvent event) {
            for (ApplicationListener listener : listeners) {
                listener.onApplicationEvent(event);
            }
        }

        public void addApplicationListener(ApplicationListener listener) {
            listeners.add(listener);
        }

        public void removeApplicationListener(ApplicationListener listener) {
            listeners.remove(listener);
        }

        public java.util.List<ApplicationListener> getListeners() {
            return java.util.Collections.unmodifiableList(listeners);
        }

        public void setListeners(java.util.List<ApplicationListener> listeners) {
            this.listeners = new java.util.ArrayList<>(listeners);
        }

        public boolean isAutoRegisterListeners() {
            return autoRegisterListeners;
        }

        public void setAutoRegisterListeners(boolean autoRegisterListeners) {
            this.autoRegisterListeners = autoRegisterListeners;
        }
    }
}

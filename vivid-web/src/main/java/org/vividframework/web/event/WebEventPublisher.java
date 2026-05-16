package org.vividframework.web.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.beans.BeanFactory;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Publishes web lifecycle events to registered WebEventListener beans.
 * Auto-discovers listeners from the bean factory.
 * @author sketch
 */
public class WebEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(WebEventPublisher.class);

    private final List<WebEventListener> listeners = new ArrayList<>();

    public WebEventPublisher() {}

    public WebEventPublisher(BeanFactory beanFactory) {
        discover(beanFactory);
    }

    /**
     * Auto-discover WebEventListener beans from the context.
     */
    public void discover(BeanFactory beanFactory) {
        if (beanFactory == null) return;
        listeners.clear();
        try {
            String[] names = beanFactory.getBeanNamesForType(WebEventListener.class);
            for (String name : names) {
                try {
                    Object bean = beanFactory.getBean(name);
                    if (bean instanceof WebEventListener listener) {
                        register(listener);
                    }
                } catch (Exception e) {
                    logger.debug("Failed to load WebEventListener: {}", name);
                }
            }
        } catch (Exception e) {
            logger.debug("No WebEventListener beans found");
        }
        listeners.sort(Comparator.comparingInt(WebEventListener::getOrder));
    }

    public void register(WebEventListener listener) {
        listeners.add(listener);
        listeners.sort(Comparator.comparingInt(WebEventListener::getOrder));
    }

    public void unregister(WebEventListener listener) {
        listeners.remove(listener);
    }

    public void publishRequestReceived(HttpServerRequest request) {
        if (listeners.isEmpty()) return;
        RequestReceivedEvent event = new RequestReceivedEvent(request);
        for (WebEventListener listener : listeners) {
            try {
                listener.onRequestReceived(event);
            } catch (Exception e) {
                logger.warn("WebEventListener.onRequestReceived failed", e);
            }
        }
    }

    public void publishRequestHandled(HttpServerRequest request, HttpServletResponse response, long elapsedMillis) {
        if (listeners.isEmpty()) return;
        RequestHandledEvent event = new RequestHandledEvent(request, response, elapsedMillis);
        for (WebEventListener listener : listeners) {
            try {
                listener.onRequestHandled(event);
            } catch (Exception e) {
                logger.warn("WebEventListener.onRequestHandled failed", e);
            }
        }
    }

    public void publishRequestFailed(HttpServerRequest request, Exception exception, long elapsedMillis) {
        if (listeners.isEmpty()) return;
        RequestFailedEvent event = new RequestFailedEvent(request, exception, elapsedMillis);
        for (WebEventListener listener : listeners) {
            try {
                listener.onRequestFailed(event);
            } catch (Exception e) {
                logger.warn("WebEventListener.onRequestFailed failed", e);
            }
        }
    }

    public List<WebEventListener> getListeners() {
        return List.copyOf(listeners);
    }
}

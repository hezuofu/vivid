package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;

import java.util.*;

/**
 * Composite implementation of HandlerMethodReturnValueHandler
 * @author sketch
 */
public class HandlerMethodReturnValueHandlerComposite {

    private final List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>();

    public HandlerMethodReturnValueHandlerComposite addHandler(HandlerMethodReturnValueHandler handler) {
        this.handlers.add(handler);
        return this;
    }

    public HandlerMethodReturnValueHandlerComposite addHandlers(List<? extends HandlerMethodReturnValueHandler> handlers) {
        this.handlers.addAll(handlers);
        return this;
    }

    public HandlerMethodReturnValueHandlerComposite addHandlers(HandlerMethodReturnValueHandler... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }

    public void clear() {
        this.handlers.clear();
    }

    public int getHandlerCount() {
        return this.handlers.size();
    }

    /**
     * Get the handler for the given return type and handler method.
     */
    public HandlerMethodReturnValueHandler getHandler(HandlerMethod handlerMethod, Class<?> returnType) {
        for (HandlerMethodReturnValueHandler handler : this.handlers) {
            if (handler.supports(handlerMethod, returnType)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Get the handler for the given return type only (backward compat).
     */
    public HandlerMethodReturnValueHandler getHandler(Class<?> returnType) {
        return getHandler(null, returnType);
    }

    /**
     * Check if any handler supports the given return type
     */
    public boolean hasHandlerFor(HandlerMethod handlerMethod, Class<?> returnType) {
        return getHandler(handlerMethod, returnType) != null;
    }

    /**
     * Handle the return value using the appropriate handler
     */
    public void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                                  HttpServerResponse response, Object returnValue) throws Exception {
        Class<?> returnType = returnValue != null ? returnValue.getClass() : void.class;
        HandlerMethodReturnValueHandler handler = getHandler(handlerMethod, returnType);

        if (handler == null) {
            throw new IllegalStateException(
                "No suitable handler found for return type: " + returnType.getName());
        }

        handler.handleReturnValue(handlerMethod, request, response, returnValue);
    }

    /**
     * Get all registered handlers
     */
    public List<HandlerMethodReturnValueHandler> getHandlers() {
        return Collections.unmodifiableList(this.handlers);
    }
}

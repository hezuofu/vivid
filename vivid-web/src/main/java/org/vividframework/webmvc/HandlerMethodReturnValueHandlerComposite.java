package org.vividframework.webmvc;

import org.vividframework.web.handler.HandlerMethod;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;

import java.util.*;

/**
 * Composite implementation of HandlerMethodReturnValueHandler
 * @author Jon Fisher
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
     * Get the handler for the given return type
     */
    public HandlerMethodReturnValueHandler getHandler(Class<?> returnType) {
        for (HandlerMethodReturnValueHandler handler : this.handlers) {
            if (handler.supports(null, returnType)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Check if any handler supports the given return type
     */
    public boolean hasHandlerFor(Class<?> returnType) {
        return getHandler(returnType) != null;
    }

    /**
     * Handle the return value using the appropriate handler
     */
    public void handleReturnValue(HandlerMethod handlerMethod, HttpServerRequest request,
                                  HttpServerResponse response, Object returnValue) throws Exception {
        Class<?> returnType = returnValue != null ? returnValue.getClass() : void.class;
        HandlerMethodReturnValueHandler handler = getHandler(returnType);

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

package org.vividframework.web.mapping;

import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.handler.HandlerExecutionChain;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handler mapping interface for mapping requests to handlers
 * @author sketch
 */
public interface HandlerMapping {

    /**
     * Get handler for the given request
     */
    HandlerExecutionChain getHandler(HttpServerRequest request) throws Exception;

    /**
     * Get the mapping comparator for ordering
     */
    default Comparator<RequestMappingInfo> getMappingComparator() {
        return null;
    }

    /**
     * Abstract base implementation
     */
    abstract class AbstractHandlerMapping implements HandlerMapping {

        @Override
        public HandlerExecutionChain getHandler(HttpServerRequest request) throws Exception {
            Object handler = getHandlerInternal(request);
            if (handler == null) {
                return null;
            }
            return new HandlerExecutionChain(handler);
        }

        protected abstract Object getHandlerInternal(HttpServerRequest request) throws Exception;
    }

    /**
     * Simple URL-based handler mapping
     */
    class SimpleUrlHandlerMapping extends AbstractHandlerMapping {

        private final Map<String, Object> urlMap = new TreeMap<>(Comparator.comparing(String::length).reversed());

        public SimpleUrlHandlerMapping() {
        }

        public SimpleUrlHandlerMapping(Map<String, ?> urlMap) {
            putAll(urlMap);
        }

        public void setMappings(Map<String, ?> mappings) {
            putAll(mappings);
        }

        public void put(String urlPath, Object handler) {
            urlMap.put(urlPath, handler);
        }

        public void putAll(Map<String, ?> mappings) {
            mappings.forEach(this::put);
        }

        @Override
        protected Object getHandlerInternal(HttpServerRequest request) throws Exception {
            String lookupPath = request.getPath();
            Object handler = urlMap.get(lookupPath);
            if (handler == null) {
                for (Map.Entry<String, Object> entry : urlMap.entrySet()) {
                    if (match(entry.getKey(), lookupPath)) {
                        return entry.getValue();
                    }
                }
            }
            return handler;
        }

        private boolean match(String pattern, String path) {
            if (pattern.equals(path)) {
                return true;
            }
            if (pattern.contains("*")) {
                String regex = pattern.replace("*", ".*");
                return path.matches(regex);
            }
            if (pattern.endsWith("/**")) {
                String prefix = pattern.substring(0, pattern.length() - 3);
                return path.startsWith(prefix);
            }
            return false;
        }

        public int getHandlerCount() {
            return urlMap.size();
        }
    }
}

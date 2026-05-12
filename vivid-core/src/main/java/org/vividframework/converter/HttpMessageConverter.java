package org.vividframework.converter;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;
import org.vividframework.http.server.HttpServerRequest;

/**
 * HTTP message converter interface for reading/writing objects
 * @author Jon Fisher
 */
public interface HttpMessageConverter<T> {

    /**
     * Check if this converter can read the given class
     */
    boolean canRead(Class<?> clazz, MediaType mediaType);

    /**
     * Check if this converter can write the given class
     */
    boolean canWrite(Class<?> clazz, MediaType mediaType);

    /**
     * Get supported media types
     */
    java.util.List<MediaType> getSupportedMediaTypes();

    /**
     * Read object from request
     */
    T read(Class<? extends T> clazz, HttpServerRequest request) throws Exception;

    /**
     * Write object to response
     */
    void write(T object, MediaType contentType, HttpServletResponse response) throws Exception;

    /**
     * Abstract base implementation
     */
    abstract class AbstractHttpMessageConverter<T> implements HttpMessageConverter<T> {

        private final MediaType defaultMediaType;
        private final java.util.List<MediaType> supportedMediaTypes;

        protected AbstractHttpMessageConverter(MediaType defaultMediaType, MediaType... supportedMediaTypes) {
            this.defaultMediaType = defaultMediaType;
            java.util.List<MediaType> types = new java.util.ArrayList<>();
            if (defaultMediaType != null) {
                types.add(defaultMediaType);
            }
            for (MediaType type : supportedMediaTypes) {
                if (!types.contains(type)) {
                    types.add(type);
                }
            }
            this.supportedMediaTypes = java.util.Collections.unmodifiableList(types);
        }

        protected AbstractHttpMessageConverter(MediaType... supportedMediaTypes) {
            this(supportedMediaTypes.length > 0 ? supportedMediaTypes[0] : null, supportedMediaTypes);
        }

        @Override
        public java.util.List<MediaType> getSupportedMediaTypes() {
            return supportedMediaTypes;
        }

        protected MediaType getDefaultMediaType() {
            return defaultMediaType;
        }

        @Override
        public boolean canRead(Class<?> clazz, MediaType mediaType) {
            return canReadClass(clazz) && supportsMediaType(mediaType);
        }

        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return canWriteClass(clazz) && supportsMediaType(mediaType);
        }

        protected boolean supportsMediaType(MediaType mediaType) {
            if (mediaType == null || mediaType.equals(MediaType.ALL)) {
                return true;
            }
            for (MediaType supported : supportedMediaTypes) {
                if (supported.isCompatibleWith(mediaType)) {
                    return true;
                }
            }
            return false;
        }

        protected abstract boolean canReadClass(Class<?> clazz);

        protected abstract boolean canWriteClass(Class<?> clazz);

        protected void addDefaultHeaders(HttpServletResponse response) {
            // Subclasses can override to add default headers
        }
    }
}

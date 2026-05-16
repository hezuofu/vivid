package org.vividframework.web.view;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.http.StreamingHttpServerResponse;

import java.util.Map;

/**
 * View interface with unified support for both buffered and streaming rendering.
 *
 * Buffered views (JsonView, RedirectView, etc.) render into a builder.
 * Streaming views (FileView, SseView) write directly to an output stream.
 *
 * @author Jon Fisher
 */
public interface View {

    /**
     * Get content type for this view.
     */
    String getContentType();

    /**
     * Whether this view uses streaming mode (chunked transfer).
     * Streaming views are rendered via {@link #renderStreaming} instead of {@link #render}.
     */
    default boolean isStreaming() {
        return false;
    }

    /**
     * Render the view into a buffered response builder.
     * Used for non-streaming views.
     */
    void render(Map<String, ?> model, HttpServerRequest request, HttpServletResponse.Builder builder) throws Exception;

    /**
     * Render the view as a streaming response.
     * Default implementation falls back to buffered rendering.
     * Streaming views must override this.
     */
    default void renderStreaming(Map<String, ?> model, HttpServerRequest request,
                                  StreamingHttpServerResponse response) throws Exception {
        // Default: render buffered then write all at once
        HttpServletResponse.Builder builder = HttpServletResponse.builder();
        render(model, request, builder);
        HttpServletResponse built = builder.build();
        response.status(built.getStatus());
        response.getHeaders().addAll(built.getHeaders());
        byte[] content = built.getContent();
        if (content != null) {
            response.getOutputStream().write(content);
        }
        response.complete();
    }

    /**
     * @deprecated Use {@link #render(Map, HttpServerRequest, HttpServletResponse.Builder)} instead.
     */
    @Deprecated
    default void render(Map<String, ?> model, HttpServerRequest request, HttpServletResponse response) throws Exception {
        HttpServletResponse.Builder builder = HttpServletResponse.builder()
                .status(response.getStatus())
                .headers(response.getHeaders())
                .content(response.getContent());
        render(model, request, builder);
    }

    /**
     * @deprecated Use builder-based or streaming render instead.
     */
    @Deprecated
    default void render(Map<String, ?> model, HttpServerRequest request, HttpServerResponse response) throws Exception {
        if (response != null) {
            render(model, request, response.toImmutable());
        }
    }

    /**
     * Abstract base for non-streaming views.
     */
    abstract class AbstractView implements View {

        private String contentType = "text/html;charset=UTF-8";

        @Override
        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public void render(Map<String, ?> model, HttpServerRequest request, HttpServletResponse.Builder builder)
                throws Exception {
            builder.contentType(getContentType());
            doRender(model, request, builder);
        }

        protected abstract void doRender(Map<String, ?> model, HttpServerRequest request,
                                          HttpServletResponse.Builder builder) throws Exception;
    }
}

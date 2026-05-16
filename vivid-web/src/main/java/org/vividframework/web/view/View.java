package org.vividframework.web.view;

import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.web.model.ModelMap;

import java.util.Map;

/**
 * View interface for rendering content
 * @author Jon Fisher
 */
public interface View {

    /**
     * Get content type
     */
    String getContentType();

    /**
     * Render the view with HttpServerResponse (for internal Netty server use)
     */
    default void render(Map<String, ?> model, HttpServerRequest request, HttpServerResponse response) throws Exception {
        // Default implementation: convert HttpServerResponse to HttpServletResponse if needed
        if (response != null) {
            render(model, request, response.toImmutable());
        }
    }

    /**
     * Render the view with HttpServletResponse (standard servlet-style response)
     */
    void render(Map<String, ?> model, HttpServerRequest request, HttpServletResponse response) throws Exception;

    /**
     * Abstract base implementation
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
        public void render(Map<String, ?> model, HttpServerRequest request, HttpServletResponse response) throws Exception {
            if (response == null) {
                throw new IllegalStateException("Response is required");
            }
            doRender(model, request, response);
        }

        @Override
        public void render(Map<String, ?> model, HttpServerRequest request, HttpServerResponse response) throws Exception {
            if (response == null) {
                throw new IllegalStateException("Response is required");
            }
            doRender(model, request, response.toImmutable());
        }

        protected abstract void doRender(Map<String, ?> model, HttpServerRequest request, HttpServletResponse response) throws Exception;

        protected HttpServletResponse getResponse() {
            return HttpServletResponse.ok();
        }
    }
}

package org.vividframework.web.view;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.StreamingHttpServerResponse;
import org.vividframework.io.Resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * View for streaming file downloads. Supports range requests via the model.
 * Uses streaming mode to avoid loading entire files into memory.
 * @author sketch
 */
public class FileView implements View {

    private final Resource resource;
    private String contentType;
    private String filename;
    private boolean inline = false;
    private long contentLength = -1;

    public FileView(Resource resource) {
        this.resource = resource;
        this.contentType = "application/octet-stream";
    }

    public FileView(Resource resource, String filename) {
        this(resource);
        this.filename = filename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    public FileView contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public FileView filename(String filename) {
        this.filename = filename;
        return this;
    }

    public FileView inline(boolean inline) {
        this.inline = inline;
        return this;
    }

    public FileView contentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    @Override
    public void render(Map<String, ?> model, HttpServerRequest request,
                       HttpServletResponse.Builder builder) throws Exception {
        // Buffered fallback: read entire resource
        builder.contentType(contentType);
        if (filename != null) {
            String disposition = inline ? "inline" : "attachment";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            builder.header(HttpHeaders.CONTENT_DISPOSITION,
                    disposition + "; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
        }
        try (InputStream in = resource.getInputStream()) {
            builder.content(in.readAllBytes());
        }
    }

    @Override
    public void renderStreaming(Map<String, ?> model, HttpServerRequest request,
                                 StreamingHttpServerResponse response) throws Exception {
        response.status(200);
        response.contentType(contentType);
        if (filename != null) {
            String disposition = inline ? "inline" : "attachment";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            response.header(HttpHeaders.CONTENT_DISPOSITION,
                    disposition + "; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded);
        }
        if (contentLength > 0) {
            response.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        }

        OutputStream out = response.getOutputStream();
        try (InputStream in = resource.getInputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                if (bytesRead == buffer.length) {
                    out.flush();
                }
            }
            out.flush();
        }
        response.complete();
    }
}

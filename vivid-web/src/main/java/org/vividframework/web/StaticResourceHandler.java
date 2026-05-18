package org.vividframework.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.*;
import org.vividframework.io.ClassPathResource;
import org.vividframework.io.Resource;
import org.vividframework.web.mapping.HandlerMapping;
import org.vividframework.web.handler.HandlerAdapter;
import org.vividframework.web.handler.HandlerExecutionChain;
import org.vividframework.web.model.ModelAndView;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Serves static resources (CSS, JS, images, fonts, etc.) from classpath.
 * Integrates as both HandlerMapping and HandlerAdapter.
 *
 * <pre>
 * StaticResourceHandler staticHandler = new StaticResourceHandler("/static/**", "static/");
 * // GET /static/css/app.css → classpath:static/css/app.css
 * </pre>
 *
 * @author sketch
 */
public class StaticResourceHandler implements HandlerMapping, HandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHandler.class);

    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("woff", "font/woff");
        MIME_TYPES.put("woff2", "font/woff2");
        MIME_TYPES.put("ttf", "font/ttf");
        MIME_TYPES.put("eot", "application/vnd.ms-fontobject");
        MIME_TYPES.put("otf", "font/otf");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("map", "application/json");
        MIME_TYPES.put("webp", "image/webp");
        MIME_TYPES.put("mp4", "video/mp4");
        MIME_TYPES.put("webm", "video/webm");
    }

    private final String urlPattern;
    private final String resourceRoot;
    private final String prefix;
    private long cacheSeconds = 3600;
    private boolean enableCaching = true;

    public StaticResourceHandler(String urlPattern, String resourceRoot) {
        this.urlPattern = urlPattern;
        this.resourceRoot = resourceRoot;
        // Extract the prefix before **
        int starIdx = urlPattern.indexOf("*");
        this.prefix = starIdx > 0 ? urlPattern.substring(0, starIdx) : urlPattern;
    }

    public StaticResourceHandler cacheSeconds(long seconds) {
        this.cacheSeconds = seconds;
        return this;
    }

    public StaticResourceHandler noCache() {
        this.enableCaching = false;
        return this;
    }

    /**
     * Set this to high order so static resources are checked after controllers.
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // Lowest priority
    }

    // --- HandlerMapping ---

    @Override
    public HandlerExecutionChain getHandler(HttpServerRequest request) {
        String path = request.getPath();
        if (!path.startsWith(prefix)) return null;

        String relativePath = path.substring(prefix.length());
        if (relativePath.isEmpty() || relativePath.contains("..")) return null;

        String resourcePath = resourceRoot + relativePath;
        try {
            Resource resource = new ClassPathResource(resourcePath);
            if (resource.exists()) {
                return new HandlerExecutionChain(new StaticResourceRequest(resource, request));
            }
        } catch (Exception e) {
            logger.debug("Static resource not found: {}", resourcePath);
        }
        return null;
    }

    // --- HandlerAdapter ---

    @Override
    public boolean supports(Object handler) {
        return handler instanceof StaticResourceRequest;
    }

    @Override
    public Object handle(HttpServerRequest request, Object handler) throws Exception {
        return handleResource((StaticResourceRequest) handler);
    }

    /**
     * Handle the resource request, returning an HttpServletResponse directly.
     */
    public HttpServletResponse handleResource(StaticResourceRequest res) {
        try {
            Resource resource = res.resource;
            String filename = getFilename(resource);
            String contentType = getContentType(filename);
            byte[] content;

            try (InputStream in = resource.getInputStream()) {
                content = in.readAllBytes();
            }

            HttpServletResponse.Builder builder = HttpServletResponse.builder()
                    .status(200)
                    .contentType(contentType)
                    .content(content);

            // Caching headers
            if (enableCaching) {
                builder.header(HttpHeaders.CACHE_CONTROL,
                        "public, max-age=" + cacheSeconds);
                builder.header(HttpHeaders.EXPIRES,
                        DateTimeFormatter.RFC_1123_DATE_TIME.format(
                                Instant.now().plusSeconds(cacheSeconds).atZone(ZoneId.of("GMT"))));
            }

            // Check If-Modified-Since for 304
            String ifModifiedSince = res.request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (ifModifiedSince != null && enableCaching) {
                builder.status(304).content(new byte[0]);
            }

            return builder.build();
        } catch (Exception e) {
            logger.debug("Error serving static resource: {}", res.resource);
            return HttpServletResponse.notFound()
                    .mutate().content("Resource not found").build();
        }
    }

    private String getFilename(Resource resource) {
        String path = resource.toString();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    public static String getContentType(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "application/octet-stream";
        String ext = filename.substring(dot + 1).toLowerCase();
        return MIME_TYPES.getOrDefault(ext, "application/octet-stream");
    }

    /**
     * Internal handler object for a static resource request.
     */
    public static class StaticResourceRequest {
        final Resource resource;
        final HttpServerRequest request;
        StaticResourceRequest(Resource resource, HttpServerRequest request) {
            this.resource = resource;
            this.request = request;
        }
    }
}

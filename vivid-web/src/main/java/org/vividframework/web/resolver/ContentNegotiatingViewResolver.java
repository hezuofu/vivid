package org.vividframework.web.resolver;

import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.view.HtmlView;
import org.vividframework.web.view.JsonView;
import org.vividframework.web.view.TextView;
import org.vividframework.web.view.View;
import org.vividframework.web.view.XmlView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves views based on Accept header or URL suffix.
 *
 * <pre>
 * GET /api/data              → Accept: application/json → JsonView
 * GET /api/data.json         → JsonView (suffix override)
 * GET /api/data?format=xml   → XmlView (param override)
 * </pre>
 *
 * @author sketch
 */
public class ContentNegotiatingViewResolver implements ViewResolver {

    private final Map<String, ViewFactory> factories = new LinkedHashMap<>();
    private String defaultFormat = "json";
    private String formatParameter = "format";

    public ContentNegotiatingViewResolver() {
        // Register default format factories
        registerFormat("json",     model -> new JsonView());
        registerFormat("html",     model -> new HtmlView(convertModelToString(model)));
        registerFormat("xml",      model -> new XmlView(convertModelToString(model)));
        registerFormat("text",     model -> new TextView(convertModelToString(model)));
    }

    /**
     * Register a format factory.
     */
    public ContentNegotiatingViewResolver registerFormat(String format, ViewFactory factory) {
        factories.put(format, factory);
        return this;
    }

    public ContentNegotiatingViewResolver defaultFormat(String format) {
        this.defaultFormat = format;
        return this;
    }

    public ContentNegotiatingViewResolver formatParameter(String param) {
        this.formatParameter = param;
        return this;
    }

    @Override
    public View resolveViewName(String viewName) throws Exception {
        return null; // This resolver works with model data, not view names
    }

    /**
     * Resolve the best view for the given model and request.
     */
    public View resolve(Map<String, ?> model, HttpServerRequest request) {
        String format = determineFormat(request);

        ViewFactory factory = factories.get(format);
        if (factory == null) {
            factory = factories.get(defaultFormat);
        }
        if (factory == null) {
            return new JsonView();
        }
        return factory.create(model);
    }

    private String determineFormat(HttpServerRequest request) {
        // 1. URL suffix (/path.json, /path.xml)
        String path = request.getPath();
        int dot = path.lastIndexOf('.');
        if (dot >= 0 && dot < path.length() - 1) {
            String suffix = path.substring(dot + 1).toLowerCase();
            if (factories.containsKey(suffix)) {
                return suffix;
            }
        }

        // 2. Query parameter (?format=xml)
        List<String> paramValues = request.getQueryParams(formatParameter);
        if (!paramValues.isEmpty()) {
            String param = paramValues.get(0).toLowerCase();
            if (factories.containsKey(param)) {
                return param;
            }
        }

        // 3. Accept header
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) {
            String resolved = resolveAcceptHeader(accept);
            if (resolved != null) return resolved;
        }

        // 4. Default
        return defaultFormat;
    }

    private String resolveAcceptHeader(String accept) {
        // Parse "text/html, application/json;q=0.9, */*;q=0.8"
        // Return first matching format
        String[] parts = accept.split(",");
        for (String part : parts) {
            String mediaType = part.split(";")[0].trim();
            for (Map.Entry<String, ViewFactory> entry : factories.entrySet()) {
                String formatMime = formatToMediaType(entry.getKey());
                if (mediaType.equals(formatMime) || mediaType.equals("*/*")) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private String formatToMediaType(String format) {
        return switch (format) {
            case "json" -> "application/json";
            case "xml"  -> "application/xml";
            case "html" -> "text/html";
            case "text" -> "text/plain";
            default     -> "application/octet-stream";
        };
    }

    private static String convertModelToString(Map<String, ?> model) {
        if (model == null || model.isEmpty()) return "";
        if (model.size() == 1) {
            Object value = model.values().iterator().next();
            return value != null ? value.toString() : "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> entry : model.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Factory for creating views from model data.
     */
    @FunctionalInterface
    public interface ViewFactory {
        View create(Map<String, ?> model);
    }
}

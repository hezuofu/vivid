package org.vividframework.web.handler;

import org.vividframework.http.HttpStatus;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RFC 7807 Problem Details for HTTP APIs.
 * Provides a standard error response format.
 *
 * <pre>
 * {
 *   "type": "https://api.example.com/errors/validation",
 *   "title": "Validation Failed",
 *   "status": 400,
 *   "detail": "Field 'email' must be a valid email address",
 *   "instance": "/api/users",
 *   "timestamp": "2024-01-01T00:00:00Z",
 *   "field": "email",
 *   "rejectedValue": "invalid"
 * }
 * </pre>
 *
 * @author sketch
 */
public class ProblemDetail {

    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private Instant timestamp;
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    private ProblemDetail(int status, String title) {
        this.status = status;
        this.title = title;
        this.timestamp = Instant.now();
    }

    // --- Factory methods ---

    public static ProblemDetail of(int status, String title) {
        return new ProblemDetail(status, title);
    }

    public static ProblemDetail badRequest(String detail) {
        return new ProblemDetail(400, "Bad Request").detail(detail);
    }

    public static ProblemDetail notFound(String detail) {
        return new ProblemDetail(404, "Not Found").detail(detail);
    }

    public static ProblemDetail unauthorized(String detail) {
        return new ProblemDetail(401, "Unauthorized").detail(detail);
    }

    public static ProblemDetail forbidden(String detail) {
        return new ProblemDetail(403, "Forbidden").detail(detail);
    }

    public static ProblemDetail conflict(String detail) {
        return new ProblemDetail(409, "Conflict").detail(detail);
    }

    public static ProblemDetail validation(String detail) {
        return new ProblemDetail(422, "Validation Failed").detail(detail);
    }

    public static ProblemDetail internalError(String detail) {
        return new ProblemDetail(500, "Internal Server Error").detail(detail);
    }

    public static ProblemDetail forException(Exception ex) {
        return new ProblemDetail(500, ex.getClass().getSimpleName())
                .detail(ex.getMessage());
    }

    public static ProblemDetail forStatus(HttpStatus status) {
        return new ProblemDetail(status.getCode(), status.getReason());
    }

    // --- Builder-style setters ---

    public ProblemDetail type(String type) { this.type = type; return this; }
    public ProblemDetail title(String title) { this.title = title; return this; }
    public ProblemDetail status(int status) { this.status = status; return this; }
    public ProblemDetail detail(String detail) { this.detail = detail; return this; }
    public ProblemDetail instance(String instance) { this.instance = instance; return this; }
    public ProblemDetail timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

    /**
     * Add an extension property (e.g., field-specific error details).
     */
    public ProblemDetail extension(String key, Object value) {
        this.extensions.put(key, value);
        return this;
    }

    /**
     * Add multiple extension properties.
     */
    public ProblemDetail extensions(Map<String, Object> extensions) {
        this.extensions.putAll(extensions);
        return this;
    }

    // --- Getters (for serialization) ---

    public String getType() { return type; }
    public String getTitle() { return title; }
    public int getStatus() { return status; }
    public String getDetail() { return detail; }
    public String getInstance() { return instance; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getExtensions() { return extensions; }
}

package org.vividframework.web.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.StreamingHttpServerResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Server-Sent Events emitter that can be returned from a controller method.
 * Provides a programmatic API for sending SSE events.
 *
 * <pre>
 * &#64;GetMapping("/events")
 * public SseEmitter streamEvents() {
 *     SseEmitter emitter = new SseEmitter();
 *     executor.execute(() -> {
 *         emitter.send(SseEvent.event("message").data("hello"));
 *         emitter.complete();
 *     });
 *     return emitter;
 * }
 * </pre>
 *
 * @author Jon Fisher
 */
public class SseEmitter implements View {

    private static final Logger logger = LoggerFactory.getLogger(SseEmitter.class);

    private final Long timeout;
    private StreamingHttpServerResponse response;
    private OutputStream outputStream;
    private volatile boolean completed;
    private final CopyOnWriteArrayList<Runnable> completionCallbacks = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<java.util.function.Consumer<Throwable>> errorCallbacks = new CopyOnWriteArrayList<>();

    public SseEmitter() {
        this(null);
    }

    public SseEmitter(Long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String getContentType() {
        return "text/event-stream;charset=UTF-8";
    }

    @Override
    public boolean isStreaming() {
        return true;
    }

    @Override
    public void render(Map<String, ?> model, HttpServerRequest request,
                       HttpServletResponse.Builder builder) {
        // SSE cannot be rendered buffered
        builder.status(500).content("SSE requires streaming response");
    }

    @Override
    public void renderStreaming(Map<String, ?> model, HttpServerRequest request,
                                 StreamingHttpServerResponse response) throws Exception {
        this.response = response;
        this.outputStream = response.getOutputStream();

        try {
            // Send initial comment to establish connection
            write(":ok\n\n");
        } catch (Exception e) {
            logger.error("SSE initialization failed", e);
        }
    }

    /**
     * Send an SSE event.
     */
    public synchronized void send(SseEvent event) throws IOException {
        if (completed) {
            throw new IllegalStateException("SseEmitter is completed");
        }
        write(event.format());
    }

    /**
     * Send a simple named event with data.
     */
    public void send(String name, Object data) throws IOException {
        send(SseEvent.event(name).data(data));
    }

    /**
     * Complete the SSE stream.
     */
    public synchronized void complete() {
        if (completed) return;
        completed = true;
        try {
            write("event: done\ndata: stream closed\n\n");
            outputStream.flush();
        } catch (Exception ignored) {
        }
        if (response != null) {
            response.complete();
        }
        completionCallbacks.forEach(Runnable::run);
    }

    /**
     * Complete with an error.
     */
    public synchronized void completeWithError(Throwable error) {
        if (completed) return;
        completed = true;
        errorCallbacks.forEach(cb -> cb.accept(error));
        if (response != null) {
            try {
                response.status(500);
                response.write("event: error\ndata: " + error.getMessage() + "\n\n");
            } catch (Exception ignored) {
            }
            response.complete();
        }
    }

    public void onCompletion(Runnable callback) {
        completionCallbacks.add(callback);
    }

    public void onError(java.util.function.Consumer<Throwable> callback) {
        errorCallbacks.add(callback);
    }

    public boolean isCompleted() {
        return completed;
    }

    private synchronized void write(String text) throws IOException {
        if (outputStream != null) {
            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    /**
     * An SSE event with id, event name, data, retry, and comment fields.
     */
    public static class SseEvent {

        private String id;
        private String name;
        private String data;
        private Long retry;
        private String comment;

        public static SseEvent event() {
            return new SseEvent();
        }

        public static SseEvent event(String name) {
            return new SseEvent().name(name);
        }

        public SseEvent id(String id) { this.id = id; return this; }
        public SseEvent name(String name) { this.name = name; return this; }
        public SseEvent data(Object data) { this.data = data != null ? data.toString() : null; return this; }
        public SseEvent retry(long millis) { this.retry = millis; return this; }
        public SseEvent comment(String comment) { this.comment = comment; return this; }

        /**
         * Format the event according to the SSE specification.
         */
        public String format() {
            StringBuilder sb = new StringBuilder();
            if (comment != null) {
                for (String line : comment.split("\n")) {
                    sb.append(": ").append(line).append("\n");
                }
            }
            if (name != null) {
                sb.append("event: ").append(name).append("\n");
            }
            if (id != null) {
                sb.append("id: ").append(id).append("\n");
            }
            if (retry != null) {
                sb.append("retry: ").append(retry).append("\n");
            }
            if (data != null) {
                for (String line : data.split("\n")) {
                    sb.append("data: ").append(line).append("\n");
                }
            }
            sb.append("\n");
            return sb.toString();
        }
    }
}

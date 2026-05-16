package org.vividframework.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.vividframework.http.HttpCookie;
import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpServerResponse;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.HttpStatus;
import org.vividframework.http.MediaType;
import org.vividframework.http.StreamingHttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty-backed streaming server response.
 * Writes directly to the Netty channel for chunked/streaming responses.
 * @author Jon Fisher
 */
public class NettyStreamingHttpServerResponse implements StreamingHttpServerResponse {

    private static final Logger logger = LoggerFactory.getLogger(NettyStreamingHttpServerResponse.class);

    private final ChannelHandlerContext ctx;
    private final FullHttpRequest nettyRequest;
    private int status = 200;
    private final HttpHeaders headers = new HttpHeaders();
    private final AtomicBoolean headersSent = new AtomicBoolean(false);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private Charset charset = StandardCharsets.UTF_8;
    private final StreamingOutputStream outputStream = new StreamingOutputStream();

    public NettyStreamingHttpServerResponse(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        this.ctx = ctx;
        this.nettyRequest = nettyRequest;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.resolve(status);
    }

    @Override
    public String getStatusMessage() {
        return getHttpStatus().getReason();
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public byte[] getBody() {
        return outputStream.toByteArray();
    }

    @Override
    public HttpServerResponse status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public HttpServerResponse status(HttpStatus status) {
        this.status = status.getCode();
        return this;
    }

    @Override
    public HttpServerResponse header(String name, String value) {
        headers.set(name, value);
        return this;
    }

    @Override
    public HttpServerResponse addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    @Override
    public HttpServerResponse contentType(MediaType contentType) {
        headers.setContentType(contentType);
        return this;
    }

    @Override
    public HttpServerResponse contentType(String contentType) {
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public HttpServerResponse charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public HttpServerResponse body(byte[] content) {
        sendHeaders();
        ctx.write(Unpooled.wrappedBuffer(content));
        return this;
    }

    @Override
    public HttpServerResponse body(String content) {
        return body(content.getBytes(charset));
    }

    @Override
    public HttpServerResponse body(String content, Charset charset) {
        return body(content.getBytes(charset));
    }

    @Override
    public HttpServerResponse json(String json) {
        contentType("application/json");
        return body(json);
    }

    @Override
    public HttpServerResponse html(String html) {
        contentType("text/html");
        return body(html);
    }

    @Override
    public HttpServerResponse text(String text) {
        contentType("text/plain");
        return body(text);
    }

    @Override
    public HttpServerResponse cookie(HttpCookie cookie) {
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return this;
    }

    @Override
    public HttpServerResponse location(String location) {
        headers.set(HttpHeaders.LOCATION, location);
        return this;
    }

    @Override
    public HttpServerResponse cacheControl(String cacheControl) {
        headers.set(HttpHeaders.CACHE_CONTROL, cacheControl);
        return this;
    }

    @Override
    public HttpServerResponse lastModified(long lastModified) {
        headers.set(HttpHeaders.LAST_MODIFIED, String.valueOf(lastModified));
        return this;
    }

    @Override
    public HttpServerResponse redirect(String url) {
        return status(302).location(url);
    }

    @Override
    public boolean isCommitted() {
        return headersSent.get();
    }

    @Override
    public HttpServletResponse toImmutable() {
        return HttpServletResponse.builder()
                .status(status)
                .headers(headers)
                .content(outputStream.toByteArray())
                .build();
    }

    @Override
    public OutputStream getOutputStream() {
        sendHeaders();
        return outputStream;
    }

    @Override
    public void flush() {
        outputStream.flushBuffer();
    }

    @Override
    public void complete() {
        if (completed.compareAndSet(false, true)) {
            flush();
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
               .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void write(byte[] data) {
        StreamingHttpServerResponse.super.write(data);
    }

    @Override
    public void write(String data) {
        StreamingHttpServerResponse.super.write(data);
    }

    private void sendHeaders() {
        if (!headersSent.compareAndSet(false, true)) {
            return;
        }
        HttpResponseStatus nettyStatus = HttpResponseStatus.valueOf(status);
        DefaultHttpResponse response = new DefaultHttpResponse(
                HttpVersion.HTTP_1_1, nettyStatus);

        // Set content type if not already set
        if (headers.getFirst(HttpHeaders.CONTENT_TYPE) == null) {
            headers.set(HttpHeaders.CONTENT_TYPE, "text/plain;charset=UTF-8");
        }

        // Set chunked transfer encoding
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        // Copy our headers to Netty headers
        for (String name : headers.keySet()) {
            for (String value : headers.get(name)) {
                response.headers().add(name, value);
            }
        }

        // Set keep-alive
        if (HttpUtil.isKeepAlive(nettyRequest)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);
    }

    /**
     * OutputStream backed by Netty channel writes.
     */
    private class StreamingOutputStream extends OutputStream {
        private ByteBuf buffer = Unpooled.buffer(8192);

        @Override
        public void write(int b) {
            buffer.writeByte(b);
            if (buffer.writableBytes() < 256) {
                flushBuffer();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) {
            int remaining = len;
            int offset = off;
            while (remaining > 0) {
                int writable = Math.min(remaining, buffer.writableBytes());
                buffer.writeBytes(b, offset, writable);
                offset += writable;
                remaining -= writable;
                if (buffer.writableBytes() < 256) {
                    flushBuffer();
                }
            }
        }

        @Override
        public void flush() {
            flushBuffer();
        }

        void flushBuffer() {
            if (buffer.readableBytes() > 0) {
                ctx.writeAndFlush(buffer.copy());
                buffer.clear();
            }
        }

        byte[] toByteArray() {
            byte[] result = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), result);
            return result;
        }
    }
}

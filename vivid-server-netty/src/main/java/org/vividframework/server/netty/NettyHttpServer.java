package org.vividframework.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateHandler;
import org.vividframework.server.AbstractHttpServer;
import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpMethod;
import org.vividframework.http.HttpRequest;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty-based HTTP server implementation
 * @author sketch
 */
public class NettyHttpServer extends AbstractHttpServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean started = new AtomicBoolean();

    public NettyHttpServer() {
        super();
    }

    public NettyHttpServer(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

    @Override
    protected void doStart() throws Exception {
        if (started.compareAndSet(false, true)) {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            configurePipeline(ch);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true);

            if (connectionTimeout > 0) {
                bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectionTimeoutUnit.toMillis(connectionTimeout));
            }

            ChannelFuture future = bootstrap.bind(host, port);
            serverChannel = future.channel();
            future.sync();
        }
    }

    private void configurePipeline(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // Idle state handler
        pipeline.addLast("idleStateHandler", new IdleStateHandler(
                0, 0, (int) connectionTimeoutUnit.toMillis(connectionTimeout), java.util.concurrent.TimeUnit.MILLISECONDS));

        // HTTP codec
        pipeline.addLast("httpCodec", new HttpServerCodec(4096, 8192, 8192));

        // Aggregate full request
        pipeline.addLast("aggregator", new HttpObjectAggregator((int) maxContentLength));

        // Custom handler
        pipeline.addLast("serverHandler", new NettyServerHandler());
    }

    @Override
    protected void doStop() throws Exception {
        if (started.compareAndSet(true, false)) {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return started.get() && serverChannel != null && serverChannel.isOpen();
    }

    /**
     * Dispatch request to handler (public wrapper for protected handleRequest).
     */
    public HttpServletResponse dispatchRequest(HttpServerRequest request) {
        return handleRequest(request);
    }

    /**
     * Dispatch streaming request to handler.
     */
    public void dispatchStreamingRequest(HttpServerRequest request,
                                          org.vividframework.http.StreamingHttpServerResponse response) {
        handleStreamingRequest(request, response);
    }

    private class NettyServerHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof FullHttpRequest httpRequest) {
                handleRequest(ctx, httpRequest);
            } else {
                super.channelRead(ctx, msg);
            }
        }

        private void handleRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
            try {
                HttpServerRequest serverRequest = convertRequest(request, ctx);

                // Use streaming mode if handler supports it
                if (NettyHttpServer.this.handler instanceof org.vividframework.http.HttpRequestStreamingHandler streamingHandler) {
                    NettyStreamingHttpServerResponse streamingResponse =
                            new NettyStreamingHttpServerResponse(ctx, request);
                    NettyHttpServer.this.dispatchStreamingRequest(serverRequest, streamingResponse);
                } else {
                    HttpServletResponse response = NettyHttpServer.this.dispatchRequest(serverRequest);
                    writeResponse(ctx, request, response);
                }
            } catch (Exception e) {
                logger.error("Error handling request", e);
                writeErrorResponse(ctx, request, 500, e.getMessage());
            }
        }

        private HttpServerRequest convertRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
            // Parse query params from URI
            Map<String, List<String>> queryParams = parseQueryParams(request.uri());

            // Parse cookies
            Map<String, String> cookies = new HashMap<>();
            String cookieHeader = request.headers().get("Cookie");
            if (cookieHeader != null) {
                for (String pair : cookieHeader.split(";")) {
                    String[] kv = pair.trim().split("=");
                    if (kv.length == 2) {
                        cookies.put(kv[0].trim(), kv[1].trim());
                    }
                }
            }

            // Get body
            byte[] body = new byte[0];
            if (request.content() != null) {
                ByteBuf content = request.content();
                if (content.isReadable()) {
                    body = new byte[content.readableBytes()];
                    content.readBytes(body);
                }
            }

            // Get addresses
            InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
            InetSocketAddress local = (InetSocketAddress) ctx.channel().localAddress();

            // Convert Netty HttpMethod to our HttpMethod
            io.netty.handler.codec.http.HttpMethod nettyMethod = request.method();
            HttpMethod method = nettyMethod != null ?
                    HttpMethod.resolve(nettyMethod.name()) : null;

            // Convert Netty headers to our headers
            HttpHeaders vfHeaders = new HttpHeaders();
            for (String name : request.headers().names()) {
                vfHeaders.add(name, request.headers().get(name));
            }

            return HttpRequest.builder()
                    .id(UUID.randomUUID().toString())
                    .method(method)
                    .uri(request.uri())
                    .headers(vfHeaders)
                    .body(body)
                    .queryParams(queryParams)
                    .cookies(cookies)
                    .remoteAddress(remote != null ? remote.getAddress().getHostAddress() : null)
                    .localAddress(local != null ? local.getAddress().getHostAddress() : null)
                    .localPort(local != null ? local.getPort() : getServerPort())
                    .protocol(request.protocolVersion().text())
                    .build();
        }

        private Map<String, List<String>> parseQueryParams(String uri) {
            Map<String, List<String>> params = new HashMap<>();
            int queryStart = uri != null ? uri.indexOf('?') : -1;
            if (queryStart >= 0) {
                String query = uri.substring(queryStart + 1);
                for (String pair : query.split("&")) {
                    int eqIndex = pair.indexOf('=');
                    if (eqIndex > 0) {
                        String key = pair.substring(0, eqIndex);
                        String value = pair.substring(eqIndex + 1);
                        params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                    }
                }
            }
            return params;
        }

        private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest request, HttpServletResponse response) {
            ByteBuf content;
            if (response.getContent() != null && response.getContent().length > 0) {
                content = ctx.alloc().buffer();
                content.writeBytes(response.getContent());
            } else {
                content = Unpooled.EMPTY_BUFFER;
            }

            FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(response.getStatus()),
                    content
            );

            // Set headers
            io.netty.handler.codec.http.HttpHeaders nettyHeaders = httpResponse.headers();
            HttpHeaders respHeaders = response.getHeaders();
            for (String name : respHeaders.keySet()) {
                nettyHeaders.set(name, respHeaders.getFirst(name));
            }

            // Set content type if not set
            if (!nettyHeaders.contains("Content-Type") && content.readableBytes() > 0) {
                nettyHeaders.set("Content-Type", "text/plain;charset=UTF-8");
            }

            // Set content length if not set
            if (!nettyHeaders.contains("Content-Length")) {
                nettyHeaders.set("Content-Length", content.readableBytes());
            }

            // Handle keep-alive
            boolean keepAlive = HttpUtil.isKeepAlive(request);
            if (keepAlive) {
                nettyHeaders.set("Connection", "keep-alive");
            }

            ChannelFuture future = ctx.writeAndFlush(httpResponse);
            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private void writeErrorResponse(ChannelHandlerContext ctx, FullHttpRequest request, int status, String message) {
            ByteBuf content = Unpooled.copiedBuffer(("Error: " + message).getBytes());

            FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(status),
                    content
            );

            httpResponse.headers().set("Content-Type", "text/plain;charset=UTF-8");
            httpResponse.headers().set("Content-Length", content.readableBytes());

            ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Exception in channel", cause);
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractHttpServer.Builder<Builder> {
        @Override
        public NettyHttpServer build() {
            NettyHttpServer server = new NettyHttpServer();
            server.host = this.host;
            server.port = this.port;
            server.contextPath = this.contextPath;
            server.handler = this.handler;
            return server;
        }
    }
}

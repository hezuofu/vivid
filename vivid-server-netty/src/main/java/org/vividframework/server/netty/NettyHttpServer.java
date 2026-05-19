package org.vividframework.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.buffer.Unpooled;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.http.HttpHeaders;
import org.vividframework.http.HttpMethod;
import org.vividframework.http.HttpRequest;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.http.HttpServletResponse;
import org.vividframework.http.MediaType;
import org.vividframework.http.StreamingHttpServerResponse;
import org.vividframework.http.HttpCookie;
import org.vividframework.server.AbstractHttpServer;
import org.vividframework.server.servlet.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty-based HTTP server — a real Servlet container.
 * Converts Netty → Jakarta Servlet, dispatches via VividServletContainer.
 *
 * @author sketch
 */
public class NettyHttpServer extends AbstractHttpServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpServer.class);

    private EventLoopGroup bossGroup, workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean started = new AtomicBoolean();
    private final VividServletContainer servletContainer = new VividServletContainer();

    public NettyHttpServer() { super(); }
    public NettyHttpServer(String host, int port) { super(); this.host = host; this.port = port; }

    public VividServletContainer getServletContainer() { return servletContainer; }

    public NettyHttpServer addServlet(String name, HttpServlet servlet, String... mappings) {
        servletContainer.addServlet(name, servlet, mappings); return this;
    }

    public NettyHttpServer addFilter(String name, Filter filter, String... mappings) {
        servletContainer.addFilter(name, filter, mappings); return this;
    }

    @Override protected void doStart() throws Exception {
        servletContainer.init();
        if (started.compareAndSet(false, true)) {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override protected void initChannel(SocketChannel ch) { configurePipeline(ch); }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
            serverChannel = b.bind(host, port).sync().channel();
            logger.info("Netty HTTP server started on {}:{}", host, port);
        }
    }

    @Override protected void doStop() {
        if (started.compareAndSet(true, false)) {
            servletContainer.destroy();
            if (serverChannel != null) serverChannel.close();
            if (bossGroup != null) bossGroup.shutdownGracefully();
            if (workerGroup != null) workerGroup.shutdownGracefully();
        }
    }

    @Override public boolean isRunning() { return started.get() && serverChannel != null && serverChannel.isOpen(); }

    public HttpServletResponse dispatchRequest(HttpServerRequest request) { return handleRequest(request); }

    private void configurePipeline(SocketChannel ch) {
        ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1048576));
        ch.pipeline().addLast(new IdleStateHandler((int) connectionTimeout, (int) connectionTimeout, 0));
        ch.pipeline().addLast(new NettyServletHandler());
    }

    private class NettyServletHandler extends ChannelInboundHandlerAdapter {
        @Override public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof FullHttpRequest nettyRequest) handle(ctx, nettyRequest);
            else ctx.fireChannelRead(msg);
        }

        private void handle(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
            boolean keepAlive = HttpUtil.isKeepAlive(nettyRequest);
            try {
                VividHttpServletRequest servletReq = buildServletRequest(nettyRequest, ctx);
                VividHttpServletResponse servletResp = new VividHttpServletResponse();

                // Session cookie handling
                for (jakarta.servlet.http.Cookie c : servletReq.getCookies()) {
                    if ("JSESSIONID".equals(c.getName())) {
                        VividHttpSession s = servletContainer.getSession(c.getValue(), false);
                        if (s != null) servletReq.setAttribute("session", s);
                    }
                }

                servletContainer.service(servletReq, servletResp);
                writeNettyResponse(ctx, servletResp, keepAlive);
            } catch (Exception e) {
                logger.error("Request error", e);
                writeError(ctx, 500, e.getMessage());
            }
        }

        private VividHttpServletRequest buildServletRequest(FullHttpRequest r, ChannelHandlerContext ctx) {
            Map<String, List<String>> qp = new HashMap<>();
            try {
                String q = new URI(r.uri()).getQuery();
                if (q != null) for (String p : q.split("&")) {
                    String[] kv = p.split("=", 2);
                    qp.computeIfAbsent(java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            k -> new ArrayList<>()).add(kv.length > 1 ? java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
                }
            } catch (Exception ignored) {}

            Map<String, String> cookies = new HashMap<>();
            String ch = r.headers().get("Cookie");
            if (ch != null) for (String p : ch.split(";")) {
                String[] kv = p.trim().split("=", 2);
                if (kv.length == 2) cookies.put(kv[0].trim(), kv[1].trim());
            }

            byte[] body = new byte[0];
            if (r.content() != null && r.content().isReadable()) {
                body = new byte[r.content().readableBytes()];
                r.content().readBytes(body);
            }

            InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
            InetSocketAddress local = (InetSocketAddress) ctx.channel().localAddress();
            HttpMethod method = HttpMethod.resolve(r.method().name());
            HttpHeaders hdrs = new HttpHeaders();
            for (String n : r.headers().names()) hdrs.add(n, r.headers().get(n));

            HttpServerRequest sr = HttpRequest.builder()
                    .id(UUID.randomUUID().toString()).method(method).uri(r.uri())
                    .headers(hdrs).body(body).queryParams(qp).cookies(cookies)
                    .remoteAddress(remote != null ? remote.getAddress().getHostAddress() : null)
                    .localAddress(local != null ? local.getAddress().getHostAddress() : null)
                    .localPort(local != null ? local.getPort() : port)
                    .protocol(r.protocolVersion().text()).build();

            return new VividHttpServletRequest(sr, servletContainer.getServletContext(),
                    "http", host != null ? host : "localhost", port);
        }

        private void writeNettyResponse(ChannelHandlerContext ctx, VividHttpServletResponse sr, boolean keepAlive) {
            DefaultFullHttpResponse nr = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(sr.getStatus()), Unpooled.wrappedBuffer(sr.getBody()));
            for (String n : sr.getHeaderNames()) for (String v : sr.getHeaders(n)) nr.headers().add(n, v);
            if (!sr.containsHeader(HttpHeaders.CONTENT_TYPE)) nr.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
            nr.headers().set(HttpHeaderNames.CONTENT_LENGTH, sr.getBody().length);
            for (Cookie c : sr.getCookies()) nr.headers().add(HttpHeaderNames.SET_COOKIE,
                    c.getName() + "=" + c.getValue() + (c.getMaxAge() > 0 ? "; Max-Age=" + c.getMaxAge() : "") + "; Path=/");
            if (keepAlive) nr.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(nr);
            if (!keepAlive) ctx.close();
        }

        private void writeError(ChannelHandlerContext ctx, int status, String msg) {
            byte[] b = msg.getBytes(StandardCharsets.UTF_8);
            DefaultFullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.valueOf(status), Unpooled.wrappedBuffer(b));
            r.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
            r.headers().set(HttpHeaderNames.CONTENT_LENGTH, b.length);
            ctx.writeAndFlush(r).addListener(ChannelFutureListener.CLOSE);
        }
    }

    // --- Legacy bridge (DispatcherHandler compatibility) ---
    public void dispatchStreamingRequest(HttpServerRequest request,
                                          org.vividframework.http.StreamingHttpServerResponse response) {
        handleStreamingRequest(request, response);
    }

    void writeResponse(ChannelHandlerContext ctx, FullHttpRequest req, HttpServletResponse resp) {
        DefaultFullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(resp.getStatus()),
                resp.getContent() != null ? Unpooled.wrappedBuffer(resp.getContent()) : Unpooled.EMPTY_BUFFER);
        for (String n : resp.getHeaders().keySet()) for (String v : resp.getHeaders().get(n)) r.headers().add(n, v);
        r.headers().set(HttpHeaderNames.CONTENT_LENGTH, r.content().readableBytes());
        ctx.writeAndFlush(r);
    }

    void writeErrorResponse(ChannelHandlerContext ctx, FullHttpRequest req, int status, String msg) {
        byte[] b = msg.getBytes(StandardCharsets.UTF_8);
        DefaultFullHttpResponse r = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(status), Unpooled.wrappedBuffer(b));
        r.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8");
        r.headers().set(HttpHeaderNames.CONTENT_LENGTH, b.length);
        ctx.writeAndFlush(r).addListener(ChannelFutureListener.CLOSE);
    }

    public static Builder builder() { return new Builder(); }
    public static class Builder extends AbstractHttpServer.Builder<Builder> {
        public NettyHttpServer build() {
            NettyHttpServer s = new NettyHttpServer();
            s.host = host; s.port = port; s.contextPath = contextPath != null ? contextPath : "";
            s.setHandler(handler); return s;
        }
    }
}

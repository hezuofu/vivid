package org.vividframework.server.netty;

import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.server.ServletContextInitializer;
import org.vividframework.server.ServletWebServer;
import org.vividframework.server.servlet.VividServletContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Netty-backed ServletWebServer implementation.
 * Wraps NettyHttpServer with the ServletWebServer abstraction.
 *
 * @author sketch
 */
public class NettyServletWebServer implements ServletWebServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServletWebServer.class);

    private final NettyHttpServer server;
    private final int port;
    private final List<ServletContextInitializer> initializers = new ArrayList<>();

    public NettyServletWebServer(NettyHttpServer server, int port) {
        this.server = server;
        this.port = port;
    }

    public NettyServletWebServer(int port) {
        this(new NettyHttpServer("0.0.0.0", port), port);
    }

    @Override
    public void start() throws Exception {
        // Run all initializers
        for (ServletContextInitializer initializer : initializers) {
            initializer.onStartup(server.getServletContainer().getServletContext());
        }
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

    @Override
    public int getPort() { return port; }

    @Override
    public boolean isRunning() { return server.isRunning(); }

    @Override
    public ServletContext getServletContext() {
        return server.getServletContainer().getServletContext();
    }

    @Override
    public VividServletContainer getServletContainer() {
        return server.getServletContainer();
    }

    @Override
    public void addInitializer(ServletContextInitializer initializer) {
        initializers.add(initializer);
    }

    /** Get the underlying Netty server. */
    public NettyHttpServer getNettyServer() { return server; }
}

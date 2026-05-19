package org.vividframework.server.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.server.ServletContextInitializer;
import org.vividframework.server.ServletWebServer;
import org.vividframework.server.ServletWebServerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory for creating NettyServletWebServer instances.
 * Equivalent to Spring Boot's TomcatServletWebServerFactory.
 *
 * <pre>
 * NettyServletWebServerFactory factory = new NettyServletWebServerFactory(8080);
 * factory.setContextPath("/app");
 * factory.addInitializers(ctx -&gt; ctx.addServlet("hello", new HelloServlet()).addMapping("/hello"));
 * ServletWebServer server = factory.getWebServer();
 * server.start();
 * </pre>
 *
 * @author sketch
 */
public class NettyServletWebServerFactory implements ServletWebServerFactory {

    private static final Logger logger = LoggerFactory.getLogger(NettyServletWebServerFactory.class);

    private int port = 8080;
    private String contextPath = "";
    private String serverHeader = "Vivid/1.0";
    private int sessionTimeout = 30; // minutes
    private boolean compression = false;
    private final List<ServletContextInitializer> initializers = new ArrayList<>();

    public NettyServletWebServerFactory() {}

    public NettyServletWebServerFactory(int port) {
        this.port = port;
    }

    public NettyServletWebServerFactory(int port, ServletContextInitializer... initializers) {
        this.port = port;
        this.initializers.addAll(Arrays.asList(initializers));
    }

    public NettyServletWebServerFactory port(int port) { this.port = port; return this; }
    public NettyServletWebServerFactory contextPath(String path) { this.contextPath = path; return this; }
    public NettyServletWebServerFactory sessionTimeout(int minutes) { this.sessionTimeout = minutes; return this; }
    public int getPort() { return port; }
    public String getContextPath() { return contextPath; }

    public void addInitializers(ServletContextInitializer... initializers) {
        this.initializers.addAll(Arrays.asList(initializers));
    }

    @Override
    public ServletWebServer getWebServer(ServletContextInitializer... additionalInitializers) {
        NettyServletWebServer server = new NettyServletWebServer(getPort());

        // Configure the context
        server.getServletContext().setSessionTimeout(sessionTimeout);

        // Register all initializers
        for (ServletContextInitializer i : initializers) {
            server.addInitializer(i);
        }
        for (ServletContextInitializer i : additionalInitializers) {
            server.addInitializer(i);
        }

        logger.info("Created NettyServletWebServer on port {}", port);
        return server;
    }
}

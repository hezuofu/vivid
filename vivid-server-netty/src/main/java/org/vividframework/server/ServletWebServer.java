package org.vividframework.server;

import jakarta.servlet.ServletContext;
import org.vividframework.server.servlet.VividServletContainer;

/**
 * Servlet-specific web server. Inspired by Spring Boot's ServletWebServer.
 * @author sketch
 */
public interface ServletWebServer extends WebServer {

    /** Get the servlet context for this server. */
    ServletContext getServletContext();

    /** Get the underlying servlet container for programmatic registration. */
    VividServletContainer getServletContainer();

    /** Add an initializer to configure servlets/filters before startup. */
    void addInitializer(ServletContextInitializer initializer);
}

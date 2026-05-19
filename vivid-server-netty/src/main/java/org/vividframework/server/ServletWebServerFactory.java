package org.vividframework.server;

/**
 * Factory for creating pre-configured ServletWebServer instances.
 * @author sketch
 */
@FunctionalInterface
public interface ServletWebServerFactory {

    /** Create a configured web server. */
    ServletWebServer getWebServer(ServletContextInitializer... initializers);
}

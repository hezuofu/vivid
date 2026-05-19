package org.vividframework.server;

/**
 * Generic web server abstraction. Inspired by Spring Boot's WebServer.
 * @author sketch
 */
public interface WebServer {

    /** Start the server. */
    void start() throws Exception;

    /** Stop the server immediately. */
    void stop() throws Exception;

    /** Get the port the server is listening on. */
    int getPort();

    /** Check if the server has been started. */
    boolean isRunning();
}

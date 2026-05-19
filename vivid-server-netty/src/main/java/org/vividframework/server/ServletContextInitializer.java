package org.vividframework.server;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * Callback for customizing a ServletContext. Equivalent to Spring Boot's
 * ServletContextInitializer.
 *
 * <pre>
 * server.addInitializer(ctx -&gt; {
 *     ctx.addServlet("myServlet", new MyServlet()).addMapping("/api/*");
 * });
 * </pre>
 *
 * @author sketch
 */
@FunctionalInterface
public interface ServletContextInitializer {

    /**
     * Configure the given servlet context.
     */
    void onStartup(ServletContext servletContext) throws ServletException;
}

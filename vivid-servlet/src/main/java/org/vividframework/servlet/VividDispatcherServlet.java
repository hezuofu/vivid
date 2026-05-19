package org.vividframework.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividframework.context.GenericApplicationContext;
import org.vividframework.http.HttpServerRequest;

import java.io.IOException;

/**
 * DispatcherServlet: the Servlet-based front controller.
 * Replaces DispatcherHandler when running in Servlet mode.
 *
 * @author sketch
 */
public class VividDispatcherServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(VividDispatcherServlet.class);

    private final GenericApplicationContext applicationContext;
    private VividServletContext servletContext;

    public VividDispatcherServlet(GenericApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public VividServletContext getServletContext() {
        if (servletContext == null) {
            servletContext = new VividServletContext(applicationContext);
        }
        return servletContext;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            if (req instanceof VividHttpServletRequest vividReq) {
                // Use the existing DispatcherHandler pipeline via the underlying request
                HttpServerRequest serverRequest = vividReq.getVividRequest();
                // Delegate to the DispatcherHandler bean
                org.vividframework.web.DispatcherHandler dispatcher =
                        applicationContext.getBean("dispatcherHandler",
                                org.vividframework.web.DispatcherHandler.class);
                org.vividframework.http.HttpServletResponse result =
                        dispatcher.handle(serverRequest);

                // Write result to servlet response
                resp.setStatus(result.getStatus());
                for (String name : result.getHeaders().keySet()) {
                    for (String value : result.getHeaders().get(name)) {
                        resp.addHeader(name, value);
                    }
                }
                byte[] body = result.getContent();
                if (body != null && body.length > 0) {
                    resp.getOutputStream().write(body);
                }
            } else {
                resp.sendError(500, "Unsupported request type");
            }
        } catch (Exception e) {
            logger.error("Error dispatching request", e);
            resp.sendError(500, e.getMessage());
        }
    }
}

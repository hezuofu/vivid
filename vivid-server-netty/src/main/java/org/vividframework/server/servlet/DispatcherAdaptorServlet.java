package org.vividframework.server.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.vividframework.http.HttpServerRequest;

import java.io.IOException;

/**
 * Bridges a handler function into the Jakarta Servlet container.
 * Registered as the default servlet ("/") in VividServletContainer.
 *
 * @author sketch
 */
public class DispatcherAdaptorServlet extends HttpServlet {

    @FunctionalInterface
    public interface HttpHandlerFunction {
        org.vividframework.http.HttpServletResponse handle(HttpServerRequest request) throws Exception;
    }

    private final HttpHandlerFunction handler;

    public DispatcherAdaptorServlet(HttpHandlerFunction handler) {
        this.handler = handler;
    }

    @Override
    protected void service(HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws IOException {
        try {
            if (req instanceof VividHttpServletRequest vividReq) {
                HttpServerRequest serverRequest = vividReq.getVividRequest();
                org.vividframework.http.HttpServletResponse result = handler.handle(serverRequest);

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
                resp.sendError(500, "Request type not supported");
            }
        } catch (Exception e) {
            resp.sendError(500, e.getMessage());
        }
    }
}

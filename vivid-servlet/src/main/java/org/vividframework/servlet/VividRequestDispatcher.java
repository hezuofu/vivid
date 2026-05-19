package org.vividframework.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Simple RequestDispatcher that forwards to a path.
 * @author sketch
 */
public class VividRequestDispatcher implements RequestDispatcher {
    private final String path;
    private final VividServletContext context;

    public VividRequestDispatcher(String path, VividServletContext context) {
        this.path = path;
        this.context = context;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
        response.getWriter().write("Forward to: " + path);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
        response.getWriter().write("Included: " + path);
    }
}

package org.vividframework.samples;

import org.vividframework.beans.annotation.Component;
import org.vividframework.http.HttpServerRequest;
import org.vividframework.web.handler.ExceptionHandler;
import org.vividframework.web.handler.ProblemDetail;
import org.vividframework.web.model.ModelAndView;

/**
 * Demonstrates the interface-based exception handler.
 * Registered automatically via @Component — no annotations needed on methods.
 */
@Component
public class SampleExceptionHandler implements ExceptionHandler<IllegalArgumentException> {

    @Override
    public Object handle(IllegalArgumentException ex, HttpServerRequest request) {
        // Return ProblemDetail for structured error responses
        return ProblemDetail.badRequest(ex.getMessage())
                .type("https://api.example.com/errors/bad-argument")
                .instance(request.getPath());
    }

    @Override
    public int getOrder() {
        return 10; // lower = higher priority
    }
}

/**
 * Catch-all handler for unhandled exceptions.
 */
@Component
class GlobalExceptionHandler implements ExceptionHandler<Exception> {

    @Override
    public Object handle(Exception ex, HttpServerRequest request) {
        // Redirect to error page for HTML, return ProblemDetail for API
        if (request.getPath().startsWith("/api/")) {
            return ProblemDetail.internalError(ex.getMessage())
                    .instance(request.getPath())
                    .extension("exceptionType", ex.getClass().getName());
        }
        return new ModelAndView("html:<h1>500 - Server Error</h1><p>{{message}}</p>")
                .addObject("message", ex.getMessage());
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // lowest priority — catch-all
    }
}

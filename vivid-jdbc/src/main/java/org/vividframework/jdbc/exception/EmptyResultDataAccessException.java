package org.vividframework.jdbc.exception;

/**
 * Exception thrown when the result of a query is expected to have exactly one result, but has zero results.
 */
public class EmptyResultDataAccessException extends RuntimeException {
    private final int expectedSize;

    public EmptyResultDataAccessException(String message, int expectedSize) {
        super(message);
        this.expectedSize = expectedSize;
    }

    public EmptyResultDataAccessException(String message, int expectedSize, Throwable cause) {
        super(message, cause);
        this.expectedSize = expectedSize;
    }

    public int getExpectedSize() {
        return expectedSize;
    }
}

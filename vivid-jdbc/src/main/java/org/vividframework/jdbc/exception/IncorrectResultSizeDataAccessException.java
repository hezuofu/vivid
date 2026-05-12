package org.vividframework.jdbc.exception;

/**
 * Exception thrown when the result of a query does not match the expected size.
 */
public class IncorrectResultSizeDataAccessException extends RuntimeException {
    private final int expectedSize;
    private final int actualSize;

    public IncorrectResultSizeDataAccessException(String message, int expectedSize, int actualSize) {
        super(message);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public IncorrectResultSizeDataAccessException(String message, int expectedSize, int actualSize, Throwable cause) {
        super(message, cause);
        this.expectedSize = expectedSize;
        this.actualSize = actualSize;
    }

    public int getExpectedSize() {
        return expectedSize;
    }

    public int getActualSize() {
        return actualSize;
    }
}

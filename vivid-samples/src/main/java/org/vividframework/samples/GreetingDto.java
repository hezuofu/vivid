package org.vividframework.samples;

public class GreetingDto {
    private String message;
    private long timestamp;

    public GreetingDto() {}

    public GreetingDto(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

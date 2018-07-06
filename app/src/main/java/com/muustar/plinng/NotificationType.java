package com.muustar.plinng;

/**
 * Készítette: feco
 * 2018.05.17.
 */
public class NotificationType {
    private String from, type, text;
    private Boolean seen;
    private long timestamp;

    public NotificationType(){}

    public NotificationType(String from, String type, Boolean seen, long timestamp) {
        this.from = from;
        this.type = type;
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public NotificationType(String from, String type, Boolean seen, long timestamp, String text) {
        this.from = from;
        this.type = type;
        this.seen = seen;
        this.timestamp = timestamp;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

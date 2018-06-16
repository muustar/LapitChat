package com.muustar.feco.mychat;

/**
 * Készítette: feco
 * 2018.05.11.
 */
public class Messages {
    private String message, type, from;
    private long time;
    private Boolean seen;
    private String nodeKey = null;
    private String edited_status = "original";  // original, edited, deleted

    public Messages() {
    }
    public Messages(String message, Boolean seen, long time, String type, String from, String
            edited_status) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;
        this.edited_status = edited_status;
    }

    public Messages(String message, Boolean seen, long time, String type, String from) {
        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;
    }



    public String getEdited_status() {
        return edited_status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }
}

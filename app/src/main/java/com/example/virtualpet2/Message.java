package com.example.virtualpet2;

import java.util.ArrayList;
import java.util.List;

public class Message {
    private String sender;
    private String message;
    private long timestamp;
    private List<String> seenBy;  // Use List instead of Set

    public Message() {
        this.seenBy = new ArrayList<>();  // Initialize the list
    }

    public Message(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.seenBy = new ArrayList<>();  // Initialize the list
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(List<String> seenBy) {
        this.seenBy = seenBy;
    }
}

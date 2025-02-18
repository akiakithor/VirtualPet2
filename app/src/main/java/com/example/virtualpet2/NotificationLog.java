package com.example.virtualpet2;

public class NotificationLog {
    private String packageName;
    private String title;
    private String text;
    private long timestamp;

    public NotificationLog() {
        // Default constructor required for calls to DataSnapshot.getValue(NotificationLog.class)
    }

    public NotificationLog(String packageName, String title, String text, long timestamp) {
        this.packageName = packageName;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

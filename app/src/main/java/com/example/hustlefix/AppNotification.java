package com.example.hustlefix;

import java.util.HashMap;
import java.util.Map;

public class AppNotification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // booking, payment, chat, emergency, general
    private String relatedId;
    private long timestamp;
    private boolean read;

    public AppNotification() {
    }

    public AppNotification(String id, String userId, String title, String message, String type, String relatedId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedId = relatedId;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("title", title);
        map.put("message", message);
        map.put("type", type);
        map.put("relatedId", relatedId);
        map.put("timestamp", timestamp);
        map.put("read", read);
        return map;
    }
}

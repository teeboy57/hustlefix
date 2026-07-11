package com.example.hustlefix;
public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String text;
    private long timestamp;
    private String status;
    private boolean isRead;
    private long readAt;
    private String type;
    public Message() {
        // Empty constructor for Firebase
    }
    public Message(String id, String senderId, String senderName, String text,
                   long timestamp, String status, boolean isRead) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.status = status;
        this.isRead = isRead;
        this.type = "text";
    }
    // Getters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public boolean isRead() { return isRead; }
    public long getReadAt() { return readAt; }
    public String getType() { return type; }
    // Setters
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setStatus(String status) { this.status = status; }
    public void setRead(boolean read) { isRead = read; }
    public void setReadAt(long readAt) { this.readAt = readAt; }
    public void setType(String type) { this.type = type; }
}
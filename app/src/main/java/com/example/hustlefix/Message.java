package com.example.hustlefix;

public class Message {
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private String messageText;
    private long timestamp;
    private boolean isRead;
    private String chatId;

    public Message() {
        // Default constructor required for Firebase
    }

    public Message(String messageId, String senderId, String senderName, String receiverId, String receiverName, String messageText) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.messageText = messageText;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
        this.chatId = generateChatId(senderId, receiverId);
    }

    private String generateChatId(String id1, String id2) {
        if (id1 == null || id2 == null) return "";
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
}
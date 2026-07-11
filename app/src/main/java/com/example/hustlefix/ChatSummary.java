package com.example.hustlefix;

public class ChatSummary {
    private String chatId;
    private String partnerId;
    private String partnerName;
    private String lastMessage;
    private long lastTimestamp;

    public ChatSummary() {}

    public ChatSummary(String chatId, String partnerId, String partnerName, String lastMessage, long lastTimestamp) {
        this.chatId = chatId;
        this.partnerId = partnerId;
        this.partnerName = partnerName;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getPartnerId() { return partnerId; }
    public void setPartnerId(String partnerId) { this.partnerId = partnerId; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public long getLastTimestamp() { return lastTimestamp; }
    public void setLastTimestamp(long lastTimestamp) { this.lastTimestamp = lastTimestamp; }
}
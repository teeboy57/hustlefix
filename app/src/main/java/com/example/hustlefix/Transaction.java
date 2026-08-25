package com.example.hustlefix;

public class Transaction {
    private String id;
    private String type;
    private Double amount;
    private Long timestamp;
    private String serviceTitle;

    public Transaction() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getAmount() { return amount != null ? amount : 0.0; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Long getTimestamp() { return timestamp != null ? timestamp : 0L; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }
}

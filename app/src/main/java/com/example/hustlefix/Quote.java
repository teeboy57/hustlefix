package com.example.hustlefix;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public class Quote {
    private String id;
    private String jobId;
    private String jobTitle;
    private String workerId;
    private String workerName;
    private String clientId;
    private String clientName;
    private String message;
    private double amount;
    private String timeline;
    private String status;
    private long timestamp;
    private long updatedAt;
    public Quote() {}
    public Quote(String jobId, String jobTitle, String workerId, String workerName,
                 String clientId, String clientName, String message, double amount, String timeline) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.workerId = workerId;
        this.workerName = workerName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.message = message;
        this.amount = amount;
        this.timeline = timeline;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getTimeline() { return timeline; }
    public void setTimeline(String timeline) { this.timeline = timeline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public String getFormattedAmount() {
        return String.format("R%.2f", amount);
    }
    public String getTimeAgo() {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        if (diff < 60000) {
            return "just now";
        } else if (diff < 3600000) {
            long minutes = diff / 60000;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (diff < 86400000) {
            long hours = diff / 3600000;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (diff < 604800000) {
            long days = diff / 86400000;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("jobId", jobId);
        map.put("jobTitle", jobTitle);
        map.put("workerId", workerId);
        map.put("workerName", workerName);
        map.put("clientId", clientId);
        map.put("clientName", clientName);
        map.put("message", message);
        map.put("amount", amount);
        map.put("timeline", timeline);
        map.put("status", status);
        map.put("timestamp", timestamp);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
package com.example.hustlefix;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

public class Job {
    private String jobId;
    private String title;
    private String category;
    private String status;
    private String clientId;
    private String workerId;
    private String clientName;
    private String workerName;
    private String location;
    private String description;
    private Double quotedAmount;
    private Long createdAt;
    private String deadline;
    private Integer applicationsCount;

    public Job() {
        // Default constructor required for Firebase
        this.status = "open";
        this.createdAt = System.currentTimeMillis();
        this.applicationsCount = 0;
    }

    public Job(String title, String category, String clientId, String clientName, String location, String description, Double quotedAmount) {
        this.title = title;
        this.category = category;
        this.clientId = clientId;
        this.clientName = clientName;
        this.location = location;
        this.description = description;
        this.quotedAmount = quotedAmount;
        this.status = "open";
        this.createdAt = System.currentTimeMillis();
        this.applicationsCount = 0;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuotedAmount() { return quotedAmount != null ? quotedAmount : 0.0; }
    public void setQuotedAmount(Double quotedAmount) { this.quotedAmount = quotedAmount; }

    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public Integer getApplicationsCount() { return applicationsCount != null ? applicationsCount : 0; }
    public void setApplicationsCount(Integer applicationsCount) { this.applicationsCount = applicationsCount; }

    @Exclude
    public String getFormattedAmount() { return String.format("R%.2f", getQuotedAmount()); }

    // Compatibility helpers
    @Exclude
    public String getPostedBy() { return clientId; }
    @Exclude
    public String getPostedByName() { return clientName; }
    @Exclude
    public String getFormattedBudget() { return getFormattedAmount(); }
    @Exclude
    public Double getBudget() { return getQuotedAmount(); }
    @Exclude
    public boolean isOwner(String userId) { return clientId != null && clientId.equals(userId); }
    @Exclude
    public Long getTimestamp() { return getCreatedAt(); }

    public static boolean isValidTransition(String currentStatus, String newStatus) {
        if (currentStatus == null || newStatus == null) return false;
        switch (currentStatus) {
            case "open":
                return newStatus.equals("cancelled") || newStatus.equals("quoted");
            case "quoted":
                return newStatus.equals("cancelled") || newStatus.equals("in-progress");
            case "in-progress":
                return newStatus.equals("cancelled") || newStatus.equals("completed");
            default:
                return false;
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("category", category);
        map.put("status", status);
        map.put("clientId", clientId);
        map.put("clientName", clientName);
        if (workerId != null) map.put("workerId", workerId);
        if (workerName != null) map.put("workerName", workerName);
        map.put("location", location);
        map.put("description", description);
        map.put("quotedAmount", quotedAmount);
        map.put("createdAt", createdAt);
        map.put("applicationsCount", applicationsCount);
        if (deadline != null) map.put("deadline", deadline);
        return map;
    }
}

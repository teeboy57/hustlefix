package com.example.hustlefix;

import com.google.firebase.database.Exclude;

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

    // Getters
    public String getJobId() { return jobId; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getClientId() { return clientId; }
    public String getWorkerId() { return workerId; }
    public String getClientName() { return clientName; }
    public String getWorkerName() { return workerName; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public Double getQuotedAmount() { return quotedAmount != null ? quotedAmount : 0.0; }
    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public String getDeadline() { return deadline; }
    public Integer getApplicationsCount() { return applicationsCount != null ? applicationsCount : 0; }

    // Setters
    public void setJobId(String jobId) { this.jobId = jobId; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }
    public void setQuotedAmount(Double quotedAmount) { this.quotedAmount = quotedAmount; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setApplicationsCount(Integer count) { this.applicationsCount = count; }

    @Exclude
    public String getFormattedAmount() { return String.format("R%.2f", getQuotedAmount()); }
    
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
}

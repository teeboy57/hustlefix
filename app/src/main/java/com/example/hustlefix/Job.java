package com.example.hustlefix;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Job {
    private String jobId;
    private String title;
    private String description;
    private double budget;
    private String location;
    private String category;
    private String deadline;
    private String status;
    private String postedBy;
    private String postedByName;
    private long timestamp;
    private Date completedDate;
    private int applicationsCount;
    private String assignedTo;
    private String assignedToName;

    public Job() {
        // Default constructor required for Firebase
        applicationsCount = 0;
        status = "open";
    }

    public Job(String title, String description, double budget, String location,
               String category, String deadline, String postedBy, String postedByName) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.location = location;
        this.category = category;
        this.deadline = deadline;
        this.postedBy = postedBy;
        this.postedByName = postedByName;
        this.timestamp = System.currentTimeMillis();
        this.status = "open";
        this.applicationsCount = 0;
    }

    // Getters and Setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getFormattedBudget() { return String.format("R%.2f", budget); }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Date getCompletedDate() { return completedDate; }
    public void setCompletedDate(Date completedDate) { this.completedDate = completedDate; }

    public int getApplicationsCount() { return applicationsCount; }
    public void setApplicationsCount(int applicationsCount) { this.applicationsCount = applicationsCount; }

    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public boolean isOwner(String userId) {
        return postedBy != null && postedBy.equals(userId);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("description", description);
        map.put("budget", budget);
        map.put("location", location);
        map.put("category", category);
        map.put("deadline", deadline);
        map.put("status", status);
        map.put("postedBy", postedBy);
        map.put("postedByName", postedByName);
        map.put("timestamp", timestamp);
        map.put("applicationsCount", applicationsCount);
        if (assignedTo != null) map.put("assignedTo", assignedTo);
        if (assignedToName != null) map.put("assignedToName", assignedToName);
        return map;
    }
}
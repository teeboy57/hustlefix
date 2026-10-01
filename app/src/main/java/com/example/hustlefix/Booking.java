package com.example.hustlefix;

import com.google.firebase.database.Exclude;

public class Booking {
    private String bookingId;
    private String jobId;
    private String clientId;
    private String clientName;
    private String workerId;
    private String workerName;
    private Double amount;
    private String status;
    private String paymentStatus;
    private Long createdAt;
    private String serviceTitle;
    private String serviceImageUrl;
    private String preferredDate;
    private String instructions;
    private String completionCode;
    private Double platformFee;
    private Double workerEarnings;
    private Long paidAt;
    private String paymentMethod;
    private Double rating;

    public Booking() {
        // Default constructor for Firebase
    }

    public Booking(String jobId, String serviceTitle, String clientId, String clientName, String workerId, String workerName, Double amount) {
        this.jobId = jobId;
        this.serviceTitle = serviceTitle;
        this.clientId = clientId;
        this.clientName = clientName;
        this.workerId = workerId;
        this.workerName = workerName;
        this.amount = amount;
        this.status = "pending";
        this.paymentStatus = "UNPAID";
        this.createdAt = System.currentTimeMillis();
        generateCompletionCode();
    }

    private void generateCompletionCode() {
        int code = (int)(Math.random() * 9000) + 1000;
        this.completionCode = String.valueOf(code);
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public String getJobId() { return jobId; }
    public String getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getWorkerId() { return workerId; }
    public String getWorkerName() { return workerName; }
    public Double getAmount() { return amount != null ? amount : 0.0; }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public String getServiceTitle() { return serviceTitle; }
    public String getServiceImageUrl() { return serviceImageUrl; }
    public String getPreferredDate() { return preferredDate; }
    public String getInstructions() { return instructions; }
    public String getCompletionCode() { return completionCode; }
    public Double getRating() { return rating != null ? rating : 0.0; }

    // Setters
    public void setBookingId(String id) { this.bookingId = id; }
    public void setJobId(String id) { this.jobId = id; }
    public void setClientId(String id) { this.clientId = id; }
    public void setClientName(String name) { this.clientName = name; }
    public void setWorkerId(String id) { this.workerId = id; }
    public void setWorkerName(String name) { this.workerName = name; }
    public void setAmount(Double amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String status) { this.paymentStatus = status; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public void setServiceTitle(String title) { this.serviceTitle = title; }
    public void setServiceImageUrl(String url) { this.serviceImageUrl = url; }
    public void setPreferredDate(String date) { this.preferredDate = date; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setCompletionCode(String code) { this.completionCode = code; }
    public void setRating(Double rating) { this.rating = rating; }

    @Exclude
    public Double getPrice() { return getAmount(); }
    @Exclude
    public Long getTimestamp() { return getCreatedAt(); }

    @Exclude
    public String getServiceTitleCompatibility() { 
        if (serviceTitle != null && !serviceTitle.isEmpty()) return serviceTitle;
        if (instructions != null && !instructions.isEmpty()) {
            return instructions.length() > 30 ? instructions.substring(0, 27) + "..." : instructions;
        }
        if (jobId != null && !jobId.isEmpty()) return "Job #" + jobId.substring(Math.max(0, jobId.length() - 6));
        return "Professional Service";
    }

    @Exclude
    public String getServiceProviderId() { return workerId; }
    @Exclude
    public String getServiceProviderName() { 
        return (workerName != null && !workerName.isEmpty()) ? workerName : "Provider"; 
    }

    @Exclude
    public String getserviceProviderId() { return workerId; }
    @Exclude
    public String getserviceProviderName() { return workerName; }
    @Exclude
    public String getserviceTitle() { return getServiceTitleCompatibility(); }
    @Exclude
    public String getserviceId() { return jobId; }
}

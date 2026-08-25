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
    private String paymentStatus; // PAID, UNPAID, PENDING
    private Long createdAt;
    private String serviceImageUrl;

    public Booking() {
        this.status = "pending";
        this.paymentStatus = "UNPAID";
        this.createdAt = System.currentTimeMillis();
    }

    public Booking(String jobId, String clientId, String clientName, String workerId, String workerName, Double amount) {
        this.jobId = jobId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.workerId = workerId;
        this.workerName = workerName;
        this.amount = amount;
        this.status = "pending";
        this.paymentStatus = "UNPAID";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public Double getAmount() { return amount != null ? amount : 0.0; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus != null ? paymentStatus : "UNPAID"; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public String getServiceImageUrl() { return serviceImageUrl; }
    public void setServiceImageUrl(String serviceImageUrl) { this.serviceImageUrl = serviceImageUrl; }

    @Exclude
    public String getFormattedAmount() { return String.format("R%.2f", getAmount()); }
    
    // Compatibility helpers (Excluded from Firebase serialization)
    @Exclude
    public String getServiceProviderId() { return workerId; }
    @Exclude
    public String getServiceProviderName() { return workerName; }
    @Exclude
    public Double getPrice() { return getAmount(); }
    @Exclude
    public Double getRating() { return 0.0; }
    @Exclude
    public Long getTimestamp() { return getCreatedAt(); }
    @Exclude
    public String getServiceTitle() { 
        if (jobId != null && !jobId.isEmpty()) {
            return "Job #" + jobId.substring(Math.max(0, jobId.length() - 5));
        }
        return "New Request";
    }
    @Exclude
    public String getServiceId() { return jobId; }
    @Exclude
    public long getBookingDate() { return createdAt; }
    @Exclude
    public String getServiceName() { return getServiceTitle(); }
    @Exclude
    public String getProviderProfileImageUrl() { return null; }
    
    // Lowercase aliases (Excluded from Firebase serialization)
    @Exclude
    public String getserviceProviderId() { return workerId; }
    @Exclude
    public String getserviceProviderName() { return workerName; }
    @Exclude
    public String getserviceTitle() { return getServiceTitle(); }
    @Exclude
    public String getserviceId() { return jobId; }
}

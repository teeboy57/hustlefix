package com.example.hustlefix;

public class Booking {
    private String bookingId;
    private String clientId;
    private String serviceProviderId;
    private String serviceId;
    private String status;
    private double price;
    private double rating;
    private String serviceName;
    private String serviceTitle;
    private String clientName;
    private String serviceProviderName;
    private long timestamp;
    private long bookingDate;
    private String serviceType;
    private String notes;
    private String providerName;

    public Booking() {}

    // ===== GETTERS =====
    public String getBookingId() { return bookingId; }
    public String getClientId() { return clientId; }
    public String getServiceProviderId() { return serviceProviderId; }
    public String getServiceId() { return serviceId; }
    public String getStatus() { return status != null ? status : "pending"; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public String getServiceName() { return serviceName != null ? serviceName : "Service"; }
    public String getServiceTitle() { return serviceTitle != null ? serviceTitle : "Service"; }
    public String getClientName() { return clientName != null ? clientName : "Client"; }
    public String getServiceProviderName() { return serviceProviderName != null ? serviceProviderName : "Provider"; }
    public long getTimestamp() { return timestamp; }
    public long getBookingDate() { return bookingDate; }
    public String getServiceType() { return serviceType != null ? serviceType : ""; }
    public String getNotes() { return notes != null ? notes : ""; }
    public String getProviderName() { return providerName != null ? providerName : "Provider"; }
    
    // Alias methods for compatibility
    @com.google.firebase.database.Exclude
    public String getserviceProviderId() { return getServiceProviderId(); }
    @com.google.firebase.database.Exclude
    public String getserviceProviderName() { return getServiceProviderName(); }
    @com.google.firebase.database.Exclude
    public String getEntrepreneurId() { return getServiceProviderId(); }
    @com.google.firebase.database.Exclude
    public String getEntrepreneurName() { return getServiceProviderName(); }

    // ===== SETTERS =====
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public void setServiceProviderId(String serviceProviderId) { this.serviceProviderId = serviceProviderId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public void setStatus(String status) { this.status = status; }
    public void setPrice(double price) { this.price = price; }
    public void setRating(double rating) { this.rating = rating; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setServiceProviderName(String serviceProviderName) { this.serviceProviderName = serviceProviderName; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setBookingDate(long bookingDate) { this.bookingDate = bookingDate; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    // Alias setters for compatibility
    @com.google.firebase.database.Exclude
    public void setserviceProviderId(String serviceProviderId) { setServiceProviderId(serviceProviderId); }
    @com.google.firebase.database.Exclude
    public void setserviceProviderName(String serviceProviderName) { setServiceProviderName(serviceProviderName); }
    public void setEntrepreneurId(String entrepreneurId) { setServiceProviderId(entrepreneurId); }
    public void setEntrepreneurName(String entrepreneurName) { setServiceProviderName(entrepreneurName); }
}
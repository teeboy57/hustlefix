package com.example.hustlefix;

public class Service {
    private String serviceId;
    private String title;
    private String description;
    private double price;
    private String category;
    private String deliveryTime;
    private String location;
    private String serviceProviderId;
    private String serviceProviderName;
    private String serviceProviderEmail;
    private String serviceProviderProfileImageUrl;
    private boolean providerVerified;
    private java.util.List<String> serviceImageUrls; // List of work photos
    private String status;
    private String availability;
    private long createdAt;
    private int bookingsCount;
    private double averageRating;

    public Service() {
        // Default constructor required for Firebase
    }

    public Service(String serviceId, String title, String description, double price,
                   String category, String deliveryTime, String location, String serviceProviderId, String serviceProviderName, String serviceProviderEmail) {
        this.serviceId = serviceId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.deliveryTime = deliveryTime;
        this.location = location;
        this.serviceProviderId = serviceProviderId;
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderEmail = serviceProviderEmail;
        this.status = "active";
        this.availability = "Available";
        this.createdAt = System.currentTimeMillis();
        this.bookingsCount = 0;
        this.averageRating = 0;
    }

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getserviceProviderId() { return serviceProviderId; }
    public void setserviceProviderId(String serviceProviderId) { this.serviceProviderId = serviceProviderId; }

    public String getserviceProviderName() { return serviceProviderName; }
    public void setserviceProviderName(String serviceProviderName) { this.serviceProviderName = serviceProviderName; }

    public String getserviceProviderEmail() { return serviceProviderEmail; }
    public void setserviceProviderEmail(String serviceProviderEmail) { this.serviceProviderEmail = serviceProviderEmail; }

    public String getServiceProviderProfileImageUrl() { return serviceProviderProfileImageUrl; }
    public void setServiceProviderProfileImageUrl(String serviceProviderProfileImageUrl) { this.serviceProviderProfileImageUrl = serviceProviderProfileImageUrl; }

    public boolean isProviderVerified() { return providerVerified; }
    public void setProviderVerified(boolean providerVerified) { this.providerVerified = providerVerified; }

    public java.util.List<String> getServiceImageUrls() { return serviceImageUrls; }
    public void setServiceImageUrls(java.util.List<String> serviceImageUrls) { this.serviceImageUrls = serviceImageUrls; }

    public String getServiceImageUrl() {
        if (serviceImageUrls != null && !serviceImageUrls.isEmpty()) {
            return serviceImageUrls.get(0);
        }
        return null;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getBookingsCount() { return bookingsCount; }
    public void setBookingsCount(int bookingsCount) { this.bookingsCount = bookingsCount; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
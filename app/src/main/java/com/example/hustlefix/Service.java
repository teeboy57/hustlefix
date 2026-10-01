package com.example.hustlefix;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;
import java.util.List;

public class Service {
    public String serviceId;
    public String title;
    public String description;
    public Double price;
    public String category;
    public String deliveryTime;
    public String location;
    public String serviceProviderId;
    public String serviceProviderName;
    public String serviceProviderEmail;
    public String serviceProviderProfileImageUrl;
    public Boolean verified;
    public List<String> serviceImageUrls;
    public String status;
    public String availability;
    public Long createdAt;
    public Integer bookingsCount;
    public Double averageRating;
    public Double latitude;
    public Double longitude;

    public Service() {
        // Default constructor for Firebase
    }

    // Getters
    public String getServiceId() { return serviceId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getPrice() { return price != null ? price : 0.0; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    
    @PropertyName("serviceProviderId")
    public String getServiceProviderId() { return serviceProviderId; }
    
    @PropertyName("serviceProviderName")
    public String getServiceProviderName() { return serviceProviderName; }
    
    @PropertyName("serviceProviderEmail")
    public String getServiceProviderEmail() { return serviceProviderEmail; }
    
    public String getServiceProviderProfileImageUrl() { return serviceProviderProfileImageUrl; }
    
    @PropertyName("verified")
    public Boolean isVerified() { return verified != null ? verified : false; }
    
    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public String getStatus() { return status; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public String getServiceImageUrl() {
        if (serviceImageUrls != null && !serviceImageUrls.isEmpty()) return serviceImageUrls.get(0);
        return null;
    }

    // Setters
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setLocation(String location) { this.location = location; }
    
    @PropertyName("serviceProviderId")
    public void setServiceProviderId(String id) { this.serviceProviderId = id; }
    
    @PropertyName("serviceProviderName")
    public void setServiceProviderName(String name) { this.serviceProviderName = name; }
    
    @PropertyName("serviceProviderEmail")
    public void setServiceProviderEmail(String email) { this.serviceProviderEmail = email; }
    
    public void setServiceProviderProfileImageUrl(String url) { this.serviceProviderProfileImageUrl = url; }
    
    @PropertyName("verified")
    public void setVerified(Boolean verified) { this.verified = verified; }
    
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public void setStatus(String status) { this.status = status; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setServiceImageUrls(List<String> urls) { this.serviceImageUrls = urls; }
    public void setLatitude(Double lat) { this.latitude = lat; }
    public void setLongitude(Double lng) { this.longitude = lng; }
    
    public List<String> getServiceImageUrls() { return serviceImageUrls; }

    // Compatibility lowercase aliases
    @Exclude
    public String getserviceId() { return serviceId; }
    @Exclude
    public String getserviceProviderId() { return serviceProviderId; }
    @Exclude
    public String getserviceProviderName() { return serviceProviderName; }
    @Exclude
    public String getserviceProviderEmail() { return serviceProviderEmail; }
}

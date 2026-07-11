package com.example.hustlefix;
public class Service {
    private String serviceId;
    private String title;
    private String description;
    private double price;
    private String category;
    private String deliveryTime;
    private String location;
    private String entrepreneurId;
    private String entrepreneurName;
    private String entrepreneurEmail;
    private String status;
    private long createdAt;
    private int bookingsCount;
    private double averageRating;
    public Service() {}
    public Service(String serviceId, String title, String description, double price, 
                   String category, String deliveryTime, String location, 
                   String entrepreneurId, String entrepreneurName, String entrepreneurEmail) {
        this.serviceId = serviceId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.deliveryTime = deliveryTime;
        this.location = location;
        this.entrepreneurId = entrepreneurId;
        this.entrepreneurName = entrepreneurName;
        this.entrepreneurEmail = entrepreneurEmail;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.bookingsCount = 0;
        this.averageRating = 0;
    }
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
    public String getEntrepreneurId() { return entrepreneurId; }
    public void setEntrepreneurId(String entrepreneurId) { this.entrepreneurId = entrepreneurId; }
    public String getEntrepreneurName() { return entrepreneurName; }
    public void setEntrepreneurName(String entrepreneurName) { this.entrepreneurName = entrepreneurName; }
    public String getEntrepreneurEmail() { return entrepreneurEmail; }
    public void setEntrepreneurEmail(String entrepreneurEmail) { this.entrepreneurEmail = entrepreneurEmail; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getBookingsCount() { return bookingsCount; }
    public void setBookingsCount(int bookingsCount) { this.bookingsCount = bookingsCount; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
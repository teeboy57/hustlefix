package com.example.hustlefix;

public class Review {
    private String reviewId;
    private String bookingId;
    private String clientId;
    private String serviceProviderId;
    private String serviceTitle;
    private String reviewDate;
    private String comment;
    private float rating;

    public Review() {}

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getServiceProviderId() { return serviceProviderId; }
    public void setServiceProviderId(String serviceProviderId) { this.serviceProviderId = serviceProviderId; }
    
    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }
    
    public String getReviewDate() { return reviewDate; }
    public void setReviewDate(String reviewDate) { this.reviewDate = reviewDate; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}
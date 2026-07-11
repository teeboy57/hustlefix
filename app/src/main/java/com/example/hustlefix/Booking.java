package com.example.hustlefix;

public class Booking {
    private String bookingId;
    private String serviceId;
    private String serviceTitle;
    private double price;
    private String clientId;
    private String clientName;
    private String entrepreneurId;
    private String entrepreneurName;
    private String status; // pending, confirmed, in_progress, completed, cancelled
    private long bookingDate;
    private long completionDate;
    private String notes;
    private double rating;

    public Booking() {}

    public Booking(String bookingId, String serviceId, String serviceTitle, double price,
                   String clientId, String clientName, String entrepreneurId, String entrepreneurName) {
        this.bookingId = bookingId;
        this.serviceId = serviceId;
        this.serviceTitle = serviceTitle;
        this.price = price;
        this.clientId = clientId;
        this.clientName = clientName;
        this.entrepreneurId = entrepreneurId;
        this.entrepreneurName = entrepreneurName;
        this.status = "pending";
        this.bookingDate = System.currentTimeMillis();
        this.completionDate = 0;
        this.notes = "";
        this.rating = 0;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceTitle() { return serviceTitle; }
    public void setServiceTitle(String serviceTitle) { this.serviceTitle = serviceTitle; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getEntrepreneurId() { return entrepreneurId; }
    public void setEntrepreneurId(String entrepreneurId) { this.entrepreneurId = entrepreneurId; }

    public String getEntrepreneurName() { return entrepreneurName; }
    public void setEntrepreneurName(String entrepreneurName) { this.entrepreneurName = entrepreneurName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getBookingDate() { return bookingDate; }
    public void setBookingDate(long bookingDate) { this.bookingDate = bookingDate; }

    public long getCompletionDate() { return completionDate; }
    public void setCompletionDate(long completionDate) { this.completionDate = completionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}
package com.example.hustlefix;
import java.util.HashMap;
import java.util.Map;
public class EmergencyRequest {
    private String id;
    private String userId;
    private String userName;
    private String userPhone;
    private String emergencyType;
    private String description;
    private double latitude;
    private double longitude;
    private String address;
    private long timestamp;
    private String status; // pending, responded, resolved
    private String responderId;
    private String responderName;
    public EmergencyRequest() {}
    public EmergencyRequest(String userId, String userName, String userPhone, String emergencyType, String description, double latitude, double longitude, String address) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.emergencyType = emergencyType;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.timestamp = System.currentTimeMillis();
        this.status = "pending";
    }
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponderId() { return responderId; }
    public void setResponderId(String responderId) { this.responderId = responderId; }
    public String getResponderName() { return responderName; }
    public void setResponderName(String responderName) { this.responderName = responderName; }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userId", userId);
        map.put("userName", userName);
        map.put("userPhone", userPhone);
        map.put("emergencyType", emergencyType);
        map.put("description", description);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("address", address);
        map.put("timestamp", timestamp);
        map.put("status", status);
        return map;
    }
}
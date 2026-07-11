package com.example.hustlefix;
public class ApiResponse {
    private String status;
    private String message;
    private String token;
    // Default constructor (required for Gson)
    public ApiResponse() {}
    // Full constructor
    public ApiResponse(String status, String message, String token) {
        this.status = status;
        this.message = message;
        this.token = token;
    }
    // Getters
    public String getStatus() {
        return status;
    }
    public String getMessage() {
        return message;
    }
    public String getToken() {
        return token;
    }
    // Setters
    public void setStatus(String status) {
        this.status = status;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setToken(String token) {
        this.token = token;
    }
    // Helper method to check success quickly
    public boolean isSuccess() {
        return status != null && status.equalsIgnoreCase("success");
    }
}

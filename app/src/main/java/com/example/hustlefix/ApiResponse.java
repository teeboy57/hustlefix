package com.example.hustlefix;

public class ApiResponse {
    private String status;
    private String message;
    private Object data;
    private int code;

    public ApiResponse() {
        // Default constructor
    }

    public ApiResponse(String status, String message, Object data, int code) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    // Helper methods
    public boolean isSuccess() {
        return "success".equals(status) || code == 200;
    }

    public boolean isError() {
        return "error".equals(status) || code >= 400;
    }
}
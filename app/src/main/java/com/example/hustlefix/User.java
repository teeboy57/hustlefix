package com.example.hustlefix;

public class User {
    private String name;
    private String email;
    private String password;
    private String role;
    private String photoURL;
    private String phone;
    private String location;
    private boolean verified;
    private boolean isSuspended;
    private Long suspensionUntil;
    private String suspensionReason;
    private long createdAt;
    private String verificationStatus;
    private String rejectionReason;
    private String adminNotes;
    private Integer reportCount;
    private boolean isFlagged;
    private Double walletBalance;
    private Double latitude;
    private Double longitude;
    private Long lastLocationUpdate;
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String branchCode;
    private String idDocumentUrl;
    private String certificateUrl;
    private Long verificationSubmittedAt;
    private Boolean verifiedEmailSent;
    private Boolean rejectionEmailSent;

    public User() {
        // Default constructor for Firebase
    }

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isVerified() { return verified; }
    public Double getWalletBalance() { return walletBalance != null ? walletBalance : 0.0; }
    public boolean isSuspended() { return isSuspended; }
    public Long getSuspensionUntil() { return suspensionUntil; }
    public String getSuspensionReason() { return suspensionReason; }
    public String getPhotoURL() { return photoURL; }
    public String getPhone() { return phone; }
    public String getLocation() { return location; }
    public long getCreatedAt() { return createdAt; }
    public String getVerificationStatus() { return verificationStatus; }
    public String getRejectionReason() { return rejectionReason; }
    public String getAdminNotes() { return adminNotes; }
    public Integer getReportCount() { return reportCount; }
    public boolean isFlagged() { return isFlagged; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getBankName() { return bankName; }
    public String getAccountHolder() { return accountHolder; }
    public String getAccountNumber() { return accountNumber; }
    public String getBranchCode() { return branchCode; }
    public String getIdDocumentUrl() { return idDocumentUrl; }
    public String getCertificateUrl() { return certificateUrl; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setWalletBalance(Double balance) { this.walletBalance = balance; }
    public void setSuspended(boolean isSuspended) { this.isSuspended = isSuspended; }
    public void setSuspensionUntil(Long suspensionUntil) { this.suspensionUntil = suspensionUntil; }
    public void setSuspensionReason(String suspensionReason) { this.suspensionReason = suspensionReason; }
    public void setPhotoURL(String url) { this.photoURL = url; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
}

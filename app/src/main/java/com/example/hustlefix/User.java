package com.example.hustlefix;

public class User {
    private String name;
    private String email;
    private String password;
    private String role;
    private String photoURL;
    private String phone;
    private String location;
    private boolean isVerified;
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

    public User() {
        // Default constructor required for Firebase/Gson
        this.createdAt = System.currentTimeMillis();
    }

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = System.currentTimeMillis();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhotoURL() { return photoURL; }
    public void setPhotoURL(String photoURL) { this.photoURL = photoURL; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public boolean isSuspended() { return isSuspended; }
    public void setSuspended(boolean suspended) { isSuspended = suspended; }

    public Long getSuspensionUntil() { return suspensionUntil; }
    public void setSuspensionUntil(Long suspensionUntil) { this.suspensionUntil = suspensionUntil; }

    public String getSuspensionReason() { return suspensionReason; }
    public void setSuspensionReason(String suspensionReason) { this.suspensionReason = suspensionReason; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public Integer getReportCount() { return reportCount != null ? reportCount : 0; }
    public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }

    public boolean isFlagged() { return isFlagged; }
    public void setFlagged(boolean flagged) { isFlagged = flagged; }

    public Double getWalletBalance() { return walletBalance != null ? walletBalance : 0.0; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }

    public Double getLatitude() { return latitude != null ? latitude : 0.0; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude != null ? longitude : 0.0; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Long getLastLocationUpdate() { return lastLocationUpdate != null ? lastLocationUpdate : 0L; }
    public void setLastLocationUpdate(Long lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getAccountHolder() { return accountHolder; }
    public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }
}

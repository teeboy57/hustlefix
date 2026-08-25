package com.example.hustlefix;
import java.util.HashMap;
import java.util.Map;
public class Worker {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String skill;
    private String category;
    private String location;
    private String profileImage;
    private String role;
    private Double rating;
    private Integer experience;
    private Integer completedJobs;
    private Boolean available;
    private String about;
    private Double hourlyRate;
    private String availability;
    private Boolean verified;
    private String rejectionReason;
    private Double latitude;
    private Double longitude;
    private Long lastLocationUpdate;
    private Map<String, Boolean> skills;
    private Map<String, Integer> ratings;

    public Worker() {
        // Default constructor for Firebase
        this.role = "worker";
        this.available = true;
        this.rating = 0.0;
        this.completedJobs = 0;
        this.skills = new HashMap<>();
        this.ratings = new HashMap<>();
    }

    public Worker(String name, String email, String phone, String skill, String category, String location) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.skill = skill;
        this.category = category;
        this.location = location;
        this.role = "worker";
        this.available = true;
        this.rating = 0.0;
        this.completedJobs = 0;
        this.experience = 0;
        this.hourlyRate = 0.0;
        this.about = "";
        this.availability = "Available";
        this.skills = new HashMap<>();
        this.ratings = new HashMap<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Double getRating() { return rating != null ? rating : 0.0; }
    public float getRatingFloat() { return getRating().floatValue(); }
    public void setRating(Double rating) { this.rating = rating; }
    public Integer getExperience() { return experience != null ? experience : 0; }
    public void setExperience(Integer experience) { this.experience = experience; }
    public Integer getCompletedJobs() { return completedJobs != null ? completedJobs : 0; }
    public void setCompletedJobs(Integer completedJobs) { this.completedJobs = completedJobs; }
    public Boolean isAvailable() { return available != null ? available : true; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    public Double getHourlyRate() { return hourlyRate != null ? hourlyRate : 0.0; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    
    public Boolean isVerified() { return verified != null ? verified : false; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getLatitude() { return latitude != null ? latitude : 0.0; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude != null ? longitude : 0.0; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Long getLastLocationUpdate() { return lastLocationUpdate != null ? lastLocationUpdate : 0L; }
    public void setLastLocationUpdate(Long lastLocationUpdate) { this.lastLocationUpdate = lastLocationUpdate; }

    public Map<String, Boolean> getSkills() { return skills; }
    public void setSkills(Map<String, Boolean> skills) { this.skills = skills; }
    public Map<String, Integer> getRatings() { return ratings; }
    public void setRatings(Map<String, Integer> ratings) { this.ratings = ratings; }
    public String getInitials() {
        if (name != null && !name.isEmpty()) {
            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                return parts[0].substring(0, 1) + parts[1].substring(0, 1);
            }
            return name.substring(0, Math.min(2, name.length()));
        }
        return "W";
    }
    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }
    public String getFormattedHourlyRate() {
        return String.format("R%.2f", hourlyRate);
    }
}
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
    private double rating;
    private int experience;
    private int completedJobs;
    private boolean available;
    private String about;
    private double hourlyRate;
    private String availability;
    private boolean verified;
    private Map<String, Boolean> skills;
    private Map<String, Integer> ratings;
    public Worker() {
        // Default constructor for Firebase
        this.role = "worker";
        this.available = true;
        this.rating = 0;
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
        this.rating = 0;
        this.completedJobs = 0;
        this.experience = 0;
        this.hourlyRate = 0;
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
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public int getCompletedJobs() { return completedJobs; }
    public void setCompletedJobs(int completedJobs) { this.completedJobs = completedJobs; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

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
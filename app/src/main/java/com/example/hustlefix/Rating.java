// Rating.java - Complete implementation
package com.example.hustlefix;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Rating {
    private String id;
    private String jobId;
    private String jobTitle;
    private String raterId;
    private String raterName;
    private String ratedId;
    private String ratedName;
    private float rating;
    private String review;
    private long timestamp;
    private boolean isAnonymous;
    private String status;

    public Rating() {}

    public Rating(String jobId, String jobTitle, String raterId, String raterName,
                  String ratedId, String ratedName, float rating, String review, boolean isAnonymous) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.raterId = raterId;
        this.raterName = raterName;
        this.ratedId = ratedId;
        this.ratedName = ratedName;
        this.rating = rating;
        this.review = review;
        this.isAnonymous = isAnonymous;
        this.timestamp = System.currentTimeMillis();
        this.status = "completed";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getRaterId() { return raterId; }
    public void setRaterId(String raterId) { this.raterId = raterId; }
    public String getRaterName() { return raterName; }
    public void setRaterName(String raterName) { this.raterName = raterName; }
    public String getRatedId() { return ratedId; }
    public void setRatedId(String ratedId) { this.ratedId = ratedId; }
    public String getRatedName() { return ratedName; }
    public void setRatedName(String ratedName) { this.ratedName = ratedName; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFormattedRating() { return String.format("%.1f", rating); }
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    public String getDisplayName() {
        if (isAnonymous) return "Anonymous User";
        return raterName != null ? raterName : "User";
    }
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("jobId", jobId);
        map.put("jobTitle", jobTitle);
        map.put("raterId", raterId);
        map.put("raterName", raterName);
        map.put("ratedId", ratedId);
        map.put("ratedName", ratedName);
        map.put("rating", rating);
        map.put("review", review);
        map.put("timestamp", timestamp);
        map.put("isAnonymous", isAnonymous);
        map.put("status", status);
        return map;
    }
}
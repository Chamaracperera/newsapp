package com.example.news_app;

public class NewsItem {
    private String id;  // Add this field
    private String title;
    private String imageUrl;
    private String category;
    private long timestamp;

    // Empty constructor for Firebase
    public NewsItem() {}

    // Add ID getter and setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Keep all other getters and setters...
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
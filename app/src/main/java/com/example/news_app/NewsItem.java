package com.example.news_app;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.HashMap;
import java.util.Map;

public class NewsItem implements Parcelable {
    private String id;
    private String title;
    private String imageUrl;
    private String category;
    private long timestamp;
    private String description;
    private int likesCount;
    private Map<String, Boolean> likedBy; // New field to track who liked the post

    // Empty constructor for Firebase
    // Empty constructor for Firebase
    public NewsItem() {
        likedBy = new HashMap<>(); // Initialize the map
    }

    protected NewsItem(Parcel in) {
        id = in.readString();
        title = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        timestamp = in.readLong();
        description = in.readString();
        likesCount = in.readInt();
    }

    public static final Creator<NewsItem> CREATOR = new Creator<NewsItem>() {
        @Override
        public NewsItem createFromParcel(Parcel in) {
            return new NewsItem(in);
        }

        @Override
        public NewsItem[] newArray(int size) {
            return new NewsItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeLong(timestamp);
        dest.writeString(description);
        dest.writeInt(likesCount);
    }
    public void mergeWith(NewsItem other) {
        if (other == null) return;

        // Update fields only if they're not null/empty in the other item
        if (other.title != null && !other.title.isEmpty()) this.title = other.title;
        if (other.imageUrl != null && !other.imageUrl.isEmpty()) this.imageUrl = other.imageUrl;
        if (other.category != null && !other.category.isEmpty()) this.category = other.category;
        if (other.description != null && !other.description.isEmpty()) this.description = other.description;
        if (other.timestamp != 0) this.timestamp = other.timestamp;
        if (other.likesCount != 0) this.likesCount = other.likesCount;
        if (other.likedBy != null && !other.likedBy.isEmpty()) {
            this.likedBy = new HashMap<>(other.likedBy);
        }
    }

    // Keep all your existing getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    // New getter and setter for likedBy
    public Map<String, Boolean> getLikedBy() {
        if (likedBy == null) {
            likedBy = new HashMap<>();
        }
        return likedBy;
    }

    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }
}
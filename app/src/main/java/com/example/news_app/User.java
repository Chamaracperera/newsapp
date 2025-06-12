package com.example.news_app;

public class User {
    public String username;
    public String email;

    public User() {
        // Required for Firebase
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}

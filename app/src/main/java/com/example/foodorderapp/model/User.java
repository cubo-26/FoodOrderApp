package com.example.foodorderapp.model;

public class User {
    private String id;
    private String email;
    private String role; // "user" or "admin"

    public User() {
        // Required for Firebase
    }

    public User(String id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

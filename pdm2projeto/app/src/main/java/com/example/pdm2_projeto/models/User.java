package com.example.pdm2_projeto.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**

 Model that represents users*/
public class User implements Serializable {
    private String id;
    private String name;
    private String email;
    private String profilePictureUrl;
    private Timestamp createdAt; // Changed from String to Timestamp

    public User() {
    }

    public User(String id, String name, String email, String profilePictureUrl, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
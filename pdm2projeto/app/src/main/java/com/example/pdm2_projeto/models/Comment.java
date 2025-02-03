package com.example.pdm2_projeto.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String id;
    private String userId;
    private String locationId;
    private String comment;
    private Timestamp createdAt;

    public Comment() {} // Required for Firestore

    public Comment(String userId, String locationId, String comment, Timestamp createdAt) {
        this.userId = userId;
        this.locationId = locationId;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() { // 🔥 Get Firestore Timestamp
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) { // 🔥 Set Firestore Timestamp
        this.createdAt = createdAt;
    }
}

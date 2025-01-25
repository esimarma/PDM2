package com.example.pdm2_projeto.models;

/**
 * Model that represents comments.
 */
public class Comment {
    private String id;
    private String userId;
    private String locationId;
    private String comment;
    private String createdAt;

    public Comment() {}

    public Comment(String id, String userId, String locationId, String comment, String createdAt) {
        this.id = id;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
package com.example.pdm2_projeto.models;

/**
 * Model that represents favorite locations.
 */
public class Favorite {
    private String id;
    private String userId;
    private String locationId;
    private String createdAt;

    public Favorite() {}

    public Favorite(String id, String userId, String locationId, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.locationId = locationId;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

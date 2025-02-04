package com.example.pdm2_projeto.models;

/**
 * Model that represents a favorite location saved by a user.
 * This class is used to store and retrieve favorite locations from a database.
 */
public class Favorite {

    /**
     * Unique identifier for the favorite entry.
     */
    private String id;

    /**
     * ID of the user who marked the location as a favorite.
     */
    private String userId;

    /**
     * ID of the location that has been marked as a favorite.
     */
    private String locationId;

    /**
     * Timestamp indicating when the location was favorited.
     */
    private String createdAt;

    /**
     * Default constructor required for database operations.
     */
    public Favorite() {}

    /**
     * Constructs a new Favorite object with the given parameters.
     *
     * @param id Unique identifier for the favorite entry.
     * @param userId ID of the user who marked the location as favorite.
     * @param locationId ID of the favorited location.
     * @param createdAt Timestamp when the favorite was added.
     */
    public Favorite(String id, String userId, String locationId, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.locationId = locationId;
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the unique ID of the favorite entry.
     *
     * @return The favorite entry ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the favorite entry.
     *
     * @param id The favorite entry ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the ID of the user who marked the location as a favorite.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who marked the location as a favorite.
     *
     * @param userId The user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the ID of the location that has been marked as a favorite.
     *
     * @return The location ID.
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * Sets the ID of the location that has been marked as a favorite.
     *
     * @param locationId The location ID.
     */
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    /**
     * Retrieves the timestamp indicating when the location was favorited.
     *
     * @return The timestamp of when the location was added to favorites.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp indicating when the location was favorited.
     *
     * @param createdAt The timestamp of when the location was added to favorites.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}


package com.example.pdm2_projeto.models;

import com.google.firebase.Timestamp;

/**
 * Represents a comment left by a user on a location.
 * This class is used for storing and retrieving comments from Firestore.
 */
public class Comment {

    /**
     * Unique identifier for the comment, assigned by Firestore.
     */
    private String id;

    /**
     * ID of the user who posted the comment.
     */
    private String userId;

    /**
     * ID of the location the comment is associated with.
     */
    private String locationId;

    /**
     * The actual text content of the comment.
     */
    private String comment;

    /**
     * Timestamp indicating when the comment was created.
     */
    private Timestamp createdAt;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public Comment() {}

    /**
     * Constructs a new Comment object with the given parameters.
     *
     * @param userId ID of the user who posted the comment.
     * @param locationId ID of the location the comment is associated with.
     * @param comment The text content of the comment.
     * @param createdAt Timestamp indicating when the comment was created.
     */
    public Comment(String userId, String locationId, String comment, Timestamp createdAt) {
        this.userId = userId;
        this.locationId = locationId;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the unique ID of the comment.
     *
     * @return The comment ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the comment.
     *
     * @param id The comment ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the ID of the user who posted the comment.
     *
     * @return The user ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who posted the comment.
     *
     * @param userId The user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the ID of the location the comment is associated with.
     *
     * @return The location ID.
     */
    public String getLocationId() {
        return locationId;
    }

    /**
     * Sets the ID of the location the comment is associated with.
     *
     * @param locationId The location ID.
     */
    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    /**
     * Retrieves the text content of the comment.
     *
     * @return The comment text.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the text content of the comment.
     *
     * @param comment The comment text.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Retrieves the timestamp indicating when the comment was created.
     *
     * @return The creation timestamp.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp indicating when the comment was created.
     *
     * @param createdAt The creation timestamp.
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

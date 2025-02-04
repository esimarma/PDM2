package com.example.pdm2_projeto.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

/**
 * Model that represents a User.
 * This class stores user information including ID, name, email, profile picture, and account creation timestamp.
 * Implements Serializable to allow object passing between activities.
 */
public class User implements Serializable {

    /**
     * Unique identifier for the user.
     */
    private String id;

    /**
     * Name of the user.
     */
    private String name;

    /**
     * Email address of the user.
     */
    private String email;

    /**
     * URL of the user's profile picture.
     */
    private String profilePictureUrl;

    /**
     * Timestamp indicating when the user account was created.
     */
    private Timestamp createdAt;

    /**
     * Default constructor required for Firestore deserialization.
     */
    public User() {}

    /**
     * Constructs a new User object with the given parameters.
     *
     * @param id Unique identifier of the user.
     * @param name Name of the user.
     * @param email Email address of the user.
     * @param profilePictureUrl URL of the user's profile picture.
     * @param createdAt Timestamp indicating when the user account was created.
     */
    public User(String id, String name, String email, String profilePictureUrl, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
        this.createdAt = createdAt;
    }

    /**
     * Retrieves the unique identifier of the user.
     *
     * @return The user ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param id The user ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Retrieves the name of the user.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the email address of the user.
     *
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the URL of the user's profile picture.
     *
     * @return The profile picture URL.
     */
    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    /**
     * Sets the URL of the user's profile picture.
     *
     * @param profilePictureUrl The profile picture URL to set.
     */
    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    /**
     * Retrieves the timestamp indicating when the user account was created.
     *
     * @return The creation timestamp.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp indicating when the user account was created.
     *
     * @param createdAt The creation timestamp to set.
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
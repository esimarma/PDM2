package com.example.pdm2_projeto.repositories;

import android.net.Uri;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class for managing user-related operations in Firestore.
 * This class provides methods to register, retrieve, update, delete users, and manage profile pictures.
 */
public class UsersRepository {

    /**
     * Firebase Authentication instance to manage user authentication.
     */
    private final FirebaseAuth auth;

    /**
     * Firestore database instance to manage user data in Firestore.
     */
    private final FirebaseFirestore db;

    /**
     * Firebase Storage instance to manage profile picture uploads.
     */
    private final FirebaseStorage storage;

    /**
     * Constructor initializes Firebase Auth, Firestore, and Firebase Storage instances.
     */
    public UsersRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Registers a user in Firestore using the User model.
     *
     * @param user     The User model to be saved.
     * @param callback Callback to indicate the success or failure of the operation.
     */
    public void registerUser(User user, FirestoreCallback<Void> callback) {
        db.collection("users")
                .document(user.getId())
                .set(user) // Uses the User model directly
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves the current user's information as a User object.
     *
     * @param callback Callback to return the user or an error.
     */
    public void getCurrentUser(FirestoreCallback<User> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("No authenticated user."));
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(userId);
                        }
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User data not found."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Returns the currently authenticated user in Firebase Auth.
     *
     * @return Authenticated user or null if not logged in.
     */
    public FirebaseUser getAuthenticatedUser() {
        return auth.getCurrentUser();
    }

    /**
     * Deletes a user document from Firestore.
     *
     * @param userId   The unique ID of the user to be deleted.
     * @param callback Callback to indicate the success or failure of the operation.
     */
    public void deleteUser(String userId, FirestoreCallback<Void> callback) {
        db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Uploads a profile picture to Firebase Storage and updates the Firestore document.
     *
     * @param imageUri The Uri of the image to upload.
     * @param callback Callback to indicate success or failure.
     */
    public void uploadProfilePicture(Uri imageUri, FirestoreCallback<String> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("No authenticated user."));
            return;
        }

        String userId = currentUser.getUid();
        StorageReference storageRef = storage.getReference().child("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            db.collection("users")
                                    .document(userId)
                                    .update("profilePictureUrl", downloadUrl)
                                    .addOnSuccessListener(aVoid -> callback.onSuccess(downloadUrl))
                                    .addOnFailureListener(callback::onFailure);
                        }))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Updates the profile picture URL of the current user in Firestore.
     *
     * @param profilePictureUrl The new profile picture URL (null to remove the picture).
     * @param callback Callback to indicate the success or failure of the operation.
     */
    public void updateProfilePicture(String profilePictureUrl, FirestoreCallback<Void> callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("No authenticated user."));
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .update("profilePictureUrl", profilePictureUrl)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Updates user details such as name and email in Firestore.
     *
     * @param userId   The ID of the user to be updated.
     * @param newName  The new name for the user.
     * @param newEmail The new email for the user.
     * @param callback Callback to indicate the success or failure of the operation.
     */
    public void updateUserDetails(String userId, String newName, String newEmail, FirestoreCallback<Void> callback) {
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("email", newEmail);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetches a user by their unique ID from Firestore.
     *
     * @param userId   The ID of the user to retrieve.
     * @param callback Callback to return the user or an error.
     */
    public void getUserById(String userId, FirestoreCallback<User> callback) {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(userId);
                        }
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}

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
 * Repository class for managing user-related operations.
 */
public class UsersRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    // Initializes Firebase Auth and Firestore
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
    public void registerUser(User user, FirestoreCallback callback) {
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
    public void getCurrentUser(FirestoreCallback callback) {
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
                        // Converts the document to a User object
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
     * @return Authenticated user or null.
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
    public void deleteUser(String userId, FirestoreCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("users") // Accesses the "users" collection in Firestore
                .document(userId) // Specifies the document to delete (by user ID)
                .delete() // Deletes the document from Firestore
                .addOnSuccessListener(aVoid -> callback.onSuccess(null)) // If successful, triggers callback
                .addOnFailureListener(callback::onFailure); // If failed, triggers callback with an error
    }

    /**
     * Uploads a profile picture to Firebase Storage and updates the Firestore document.
     *
     * @param imageUri The Uri of the image to upload.
     * @param callback Callback to indicate success or failure.
     */
    public void uploadProfilePicture(Uri imageUri, FirestoreCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onFailure(new Exception("Usuário não autenticado."));
            return;
        }

        String userId = currentUser.getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_pictures/" + userId + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            FirebaseFirestore.getInstance().collection("users")
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
    public void updateProfilePicture(String profilePictureUrl, FirestoreCallback callback) {
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
    public void updateUserDetails(String userId, String newName, String newEmail, FirestoreCallback<Void> callback) {
        DocumentReference userRef = db.collection("users").document(userId); // Use 'db' instead of 'firestore'

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("email", newEmail);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

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
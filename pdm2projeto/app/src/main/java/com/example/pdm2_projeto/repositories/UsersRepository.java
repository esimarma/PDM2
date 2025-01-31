package com.example.pdm2_projeto.repositories;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Repository class for managing user-related operations.
 */
public class UsersRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    // Initializes Firebase Auth and Firestore
    public UsersRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
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


}
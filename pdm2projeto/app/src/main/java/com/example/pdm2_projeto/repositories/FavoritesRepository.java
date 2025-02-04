package com.example.pdm2_projeto.repositories;

import android.util.Log;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Favorite;
import com.example.pdm2_projeto.models.Location;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Repository class for managing user favorites in Firestore.
 * This class handles the addition, removal, and retrieval of favorite locations for authenticated users.
 *
 * It interacts with Firebase Authentication to get the current logged-in user and uses Firestore
 * to store and manage the user's favorite locations.
 */
public class FavoritesRepository {

    /**
     * Firebase Authentication instance to get the currently authenticated user.
     */
    private final FirebaseAuth auth;

    /**
     * Firestore database instance to manage the "favorites" collection.
     */
    private final FirebaseFirestore db;

    /**
     * Constructor that initializes Firebase Authentication and Firestore instances.
     * These instances will be used to interact with Firestore and retrieve authentication details.
     */
    public FavoritesRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface for retrieving a list of favorite locations.
     * Used to return a list of Favorite objects or handle errors when fetching user favorites.
     */
    public interface FavoritesCallback {
        void onSuccess(List<Favorite> favorites);
        void onFailure(Exception e);
    }

    /**
     * Callback interface for operations that return a single Favorite object.
     * Used when a specific favorite entry needs to be retrieved.
     */
    public interface SingleFavoritesCallback {
        void onSuccess(Favorite favorite);
        void onFailure(Exception e);
    }

    /**
     * Adds a location to the user's list of favorite locations in Firestore.
     *
     * @param locationId The unique ID of the location to be added as a favorite.
     * @param callback   Callback to handle success or failure of the operation.
     */
    public void addFavorite(String locationId, FirestoreCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid(); // Get the current user's UID
        if (userId == null) {
            callback.onFailure(new Exception("No authenticated user.")); // Fail if no user is authenticated
            return;
        }

        String favoriteId = db.collection("favorites").document().getId(); // Generate a unique ID for the favorite entry
        Favorite favorite = new Favorite(favoriteId, userId, locationId, String.valueOf(System.currentTimeMillis()));

        db.collection("favorites")
                .document(favoriteId)
                .set(favorite)
                .addOnSuccessListener(unused -> callback.onSuccess(null)) // Successfully added favorite
                .addOnFailureListener(callback::onFailure); // Handle failure
    }

    /**
     * Removes a location from the user's favorites list in Firestore.
     *
     * @param locationId The unique ID of the location to be removed from favorites.
     * @param callback   Callback to handle success or failure of the operation.
     */
    public void removeFavorite(String locationId, FirestoreCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid(); // Get the current user's UID
        if (userId == null) {
            callback.onFailure(new Exception("No authenticated user.")); // Fail if no user is authenticated
            return;
        }

        // Query Firestore to find the favorite entry for the user and location
        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("locationId", locationId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(unused -> callback.onSuccess(null)) // Successfully removed favorite
                                .addOnFailureListener(callback::onFailure); // Handle failure
                    }
                })
                .addOnFailureListener(callback::onFailure); // Handle failure in fetching favorites
    }

    /**
     * Retrieves a list of all favorite locations for the currently authenticated user.
     *
     * @param callback Callback to return the list of favorites or handle errors.
     */
    public void getUserFavorites(FavoritesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("No authenticated user.")); // Fail if no user is authenticated
            return;
        }

        String userId = auth.getCurrentUser().getUid(); // Get the current user's UID

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Favorite> favorites = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String id = document.getId();
                                String locationId = document.getString("locationId");
                                String createdAt = document.getString("createdAt");

                                if (locationId != null && createdAt != null) {
                                    favorites.add(new Favorite(id, userId, locationId, createdAt)); // Add favorite to the list
                                } else {
                                    Log.w("FavoritesRepository", "Skipping document due to missing required fields: " + document.getId());
                                }
                            } catch (Exception e) {
                                Log.e("FavoritesRepository", "Error parsing favorite document: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(favorites);
                    } else {
                        Exception e = task.getException();
                        Log.e("FavoritesRepository", "Error fetching favorites", e);
                        callback.onFailure(e);
                    }
                });
    }

    /**
     * Checks if a specific location is favorited by the currently authenticated user.
     *
     * @param locationId The ID of the location to check.
     * @return Task that resolves to true if the location is favorited, false otherwise.
     */
    public Task<Boolean> isLocationFavorited(String locationId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No authenticated user.")); // Fail if no user is authenticated
        }

        String userId = currentUser.getUid(); // Get the current user's UID

        return db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("locationId", locationId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty(); // Return true if there are results
                    } else {
                        throw task.getException(); // Handle query failure
                    }
                });
    }
}

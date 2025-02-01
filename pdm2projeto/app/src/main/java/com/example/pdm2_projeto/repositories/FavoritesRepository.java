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

public class FavoritesRepository {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    public FavoritesRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }
    public interface FavoritesCallback {
        void onSuccess(List<Favorite> favorites);
        void onFailure(Exception e);
    }
    /**
     * Callback interface for operations that return a single Location object.
     */
    public interface SingleFavoritesCallback {
        void onSuccess(Favorite favorite);
        void onFailure(Exception e);
    }

    public void addFavorite(String locationId, FirestoreCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();
        if (userId == null) {
            callback.onFailure(new Exception("No authenticated user."));
            return;
        }

        String favoriteId = db.collection("favorites").document().getId();
        Favorite favorite = new Favorite(favoriteId, userId, locationId, String.valueOf(System.currentTimeMillis()));

        db.collection("favorites")
                .document(favoriteId)
                .set(favorite)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void removeFavorite(String locationId, FirestoreCallback<Void> callback) {
        String userId = auth.getCurrentUser().getUid();
        if (userId == null) {
            callback.onFailure(new Exception("No authenticated user."));
            return;
        }

        db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("locationId", locationId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(unused -> callback.onSuccess(null))
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getUserFavorites(FavoritesCallback callback) {
        if (auth.getCurrentUser() == null) {
            callback.onFailure(new Exception("No authenticated user."));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

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
                                    favorites.add(new Favorite(id, userId, locationId, createdAt));
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


    public Task<Boolean> isLocationFavorited(String locationId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return Tasks.forException(new Exception("No authenticated user."));
        }

        String userId = currentUser.getUid(); // Obtém o UID do usuário autenticado

        return db.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("locationId", locationId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty();
                    } else {
                        throw task.getException();
                    }
                });
    }
}
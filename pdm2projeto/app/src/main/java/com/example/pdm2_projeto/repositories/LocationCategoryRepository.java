package com.example.pdm2_projeto.repositories;

import android.util.Log;

import com.example.pdm2_projeto.models.LocationCategory;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing location categories in Firestore.
 * This class allows fetching location categories stored in Firestore.
 */
public class LocationCategoryRepository {

    /**
     * Reference to the Firestore collection where location categories are stored.
     */
    private final CollectionReference categoryCollection;

    /**
     * Callback interface for retrieving location categories.
     * Provides success and failure methods for handling Firestore operations.
     */
    public interface CategoryCallback {
        void onSuccess(List<LocationCategory> categories);
        void onFailure(Exception e);
    }

    /**
     * Constructor that initializes the Firestore database and sets the reference to the "location_category" collection.
     */
    public LocationCategoryRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        categoryCollection = db.collection("location_category");
    }

    /**
     * Fetches all location categories from Firestore.
     *
     * @param callback Callback to handle the retrieved list of categories or failure.
     */
    public void getCategories(CategoryCallback callback) {
        categoryCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<LocationCategory> categories = new ArrayList<>();

                        // Loop through Firestore documents and convert them into LocationCategory objects
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                LocationCategory locationCategory = document.toObject(LocationCategory.class);

                                // Only add the category if it contains a valid description
                                if (locationCategory.getDescription() != null) {
                                    categories.add(locationCategory);
                                } else {
                                    Log.w("LocationCategoryRepo", "Skipping document due to missing 'description': " + document.getId());
                                }
                            } catch (Exception e) {
                                Log.e("LocationCategoryRepo", "Error processing category document: " + document.getId(), e);
                            }
                        }

                        // Return the successfully retrieved categories
                        callback.onSuccess(categories);
                    } else {
                        Exception e = task.getException();
                        Log.e("LocationCategoryRepo", "Error fetching categories from Firestore", e);
                        callback.onFailure(e);
                    }
                });
    }
}


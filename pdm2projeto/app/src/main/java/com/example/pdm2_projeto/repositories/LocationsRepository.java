package com.example.pdm2_projeto.repositories;

import com.example.pdm2_projeto.models.Location;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing Location objects stored in Firestore.
 */
public class LocationsRepository {

    // Firestore collection reference for locations
    private final CollectionReference locationCollection;

    /**
     * Constructor initializes the Firestore instance and points to the "locations" collection.
     */
    public LocationsRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.locationCollection = db.collection("locations");
    }

    /**
     * Fetches all Location objects from the Firestore database.
     *
     * @param callback Callback interface to handle the results or errors.
     */
    public void getAllLocations(LocationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Location> locations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String id = document.getString("id");
                                String name = document.getString("name");
                                String description = document.getString("description");
                                String address = document.getString("address");
                                String categoryId = document.getString("categoryId");
                                String createdAt = document.getString("createdAt");

                                // Tratamento de latitude e longitude
                                double latitude = 0.0;
                                double longitude = 0.0;
                                try {
                                    latitude = document.getDouble("latitude");
                                    longitude = document.getDouble("longitude");
                                } catch (ClassCastException e) {
                                    // Tenta converter caso os valores sejam String
                                    latitude = Double.parseDouble(document.getString("latitude"));
                                    longitude = Double.parseDouble(document.getString("longitude"));
                                }

                                locations.add(new Location(id, name, description, address, latitude, longitude, categoryId, createdAt));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        callback.onSuccess(locations);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Fetches a single Location by its ID from the Firestore database.
     *
     * @param id       The unique ID of the Location document.
     * @param callback Callback interface to handle the result or errors.
     */
    public void getLocationById(String id, final SingleLocationCallback callback) {
        locationCollection
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Location location = documentSnapshot.toObject(Location.class);
                        if (location != null) {
                            location.setId(documentSnapshot.getId()); // Set the Firestore document ID
                            callback.onSuccess(location);
                        } else {
                            callback.onFailure(new Exception("Location not found"));
                        }
                    } else {
                        callback.onFailure(new Exception("Document does not exist"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a new Location to the Firestore database.
     *
     * @param location The Location object to be added.
     * @param callback Callback interface to handle success or errors.
     */
    public void addLocation(Location location, final OperationCallback callback) {
        locationCollection
                .add(location)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Updates an existing Location in the Firestore database.
     *
     * @param location The Location object with updated data.
     * @param callback Callback interface to handle success or errors.
     */
    public void updateLocation(Location location, final OperationCallback callback) {
        locationCollection
                .document(location.getId())
                .set(location)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Deletes a Location from the Firestore database.
     *
     * @param id       The ID of the Location document to delete.
     * @param callback Callback interface to handle success or errors.
     */
    public void deleteLocation(String id, final OperationCallback callback) {
        locationCollection
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Callback interface for operations that return a list of Location objects.
     */
    public interface LocationCallback {
        void onSuccess(List<Location> locations);
        void onFailure(Exception e);
    }

    /**
     * Callback interface for operations that return a single Location object.
     */
    public interface SingleLocationCallback {
        void onSuccess(Location location);
        void onFailure(Exception e);
    }

    /**
     * Callback interface for operations that perform a single success or failure action.
     */
    public interface OperationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}

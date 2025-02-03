package com.example.pdm2_projeto.repositories;

import android.util.Log;

import com.example.pdm2_projeto.models.Location;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
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
        locationCollection.orderBy("name").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Location> locations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String id = document.getId(); // Firestore Document ID as fallback
                                String name = document.getString("name");
                                String description = document.getString("description");
                                String address = document.getString("address");
                                String categoryId = document.getString("categoryId");
                                String imageUrl = document.getString("imageUrl");
                                String country = document.getString("country");

                                // Tratamento de latitude e longitude
                                double latitude = document.contains("latitude") ? document.getDouble("latitude") : 0.0;
                                double longitude = document.contains("longitude") ? document.getDouble("longitude") : 0.0;

                                // Verifica campos obrigatórios antes de adicionar à lista
                                if (name != null && description != null) {
                                    locations.add(new Location(id, name, description, address, latitude, longitude, categoryId, imageUrl, country));
                                } else {
                                    Log.w("LocationsRepository", "Skipping document due to missing required fields: " + document.getId());
                                }
                            } catch (Exception e) {
                                Log.e("LocationsRepository", "Error parsing location document: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(locations);
                    } else {
                        Exception e = task.getException();
                        Log.e("LocationsRepository", "Error fetching locations", e);
                        callback.onFailure(e);
                    }
                });
    }

    public DocumentSnapshot lastDocumentSnapshot = null; // Mantém a referência do último documento

    public void getPaginatedLocations(int pageSize, LocationCallback callback) {
        Query query = locationCollection.orderBy("name").limit(pageSize);

        if (lastDocumentSnapshot != null) {
            query = query.startAfter(lastDocumentSnapshot);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Location> locations = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        String id = document.getId();
                        String name = document.getString("name");
                        String description = document.getString("description");
                        String address = document.getString("address");
                        String categoryId = document.getString("categoryId");
                        String imageUrl = document.getString("imageUrl");
                        String country = document.getString("country");
                        double latitude = document.contains("latitude") ? document.getDouble("latitude") : 0.0;
                        double longitude = document.contains("longitude") ? document.getDouble("longitude") : 0.0;

                        if (name != null && description != null) {
                            locations.add(new Location(id, name, description, address, latitude, longitude, categoryId, imageUrl, country));
                        }
                    } catch (Exception e) {
                        Log.e("LocationsRepository", "Erro ao analisar o documento: " + document.getId(), e);
                    }
                }

                if (!task.getResult().isEmpty()) {
                    lastDocumentSnapshot = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }

                callback.onSuccess(locations);
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    // Método para calcular a distância entre dois pontos (Haversine)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raio da Terra em km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public void getLocationsByCategory(String categoryId, LocationCallback callback) {
        locationCollection.whereEqualTo("category_id", categoryId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Location> locations = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String id = document.getId();
                                String name = document.getString("name");
                                String description = document.getString("description");
                                String address = document.getString("address");
                                String categoryIdDb = document.getString("category_id");
                                String imageUrl = document.getString("imageUrl");
                                String country = document.getString("country");
                                double latitude = document.contains("latitude") ? document.getDouble("latitude") : 0.0;
                                double longitude = document.contains("longitude") ? document.getDouble("longitude") : 0.0;

                                if (name != null && description != null) {
                                    locations.add(new Location(id, name, description, address, latitude, longitude, categoryIdDb, imageUrl, country));
                                }
                            } catch (Exception e) {
                                Log.e("LocationsRepository", "Erro ao processar localizações", e);
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

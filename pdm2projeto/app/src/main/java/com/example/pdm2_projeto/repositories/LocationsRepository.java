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
/**
 * Repository class for managing locations in Firestore.
 * This class provides methods to fetch all locations, retrieve paginated results,
 * and filter locations based on a specific category.
 */
public class LocationsRepository {

    /**
     * Reference to the Firestore collection where location data is stored.
     */
    private final CollectionReference locationCollection;

    /**
     * Keeps track of the last document snapshot retrieved in paginated queries.
     * Used to determine where the next batch of results should start.
     */
    public DocumentSnapshot lastDocumentSnapshot = null;

    /**
     * Constructor initializes the Firestore instance and points to the "locations" collection.
     */
    public LocationsRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.locationCollection = db.collection("locations");
    }

    /**
     * Fetches all Location objects from the Firestore database.
     * Results are ordered alphabetically by name.
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
                                Location location = document.toObject(Location.class);

                                // Ensure required fields are present before adding to the list
                                if (location.getName() != null && location.getDescription() != null) {
                                    locations.add(location);
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

    /**
     * Fetches a paginated list of Locations from Firestore.
     *
     * This method retrieves a fixed number of locations, sorted alphabetically by name,
     * starting after the last retrieved document for pagination purposes.
     *
     * @param pageSize  The number of locations to fetch per request.
     * @param callback  Callback interface to handle the result or errors.
     */
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
                        Location location = document.toObject(Location.class);

                        if (location.getName() != null && location.getDescription() != null) {
                            locations.add(location);
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


    /**
     * Fetches a paginated list of Locations from Firestore filtered by category.
     *
     * This method retrieves a set number of locations that belong to a specific category,
     * starting after the last retrieved document for pagination.
     *
     * @param categoryId The unique ID of the category to filter locations.
     * @param searchQuery The search query to filter locations.
     * @param pageSize   The number of locations to fetch per request.
     * @param callback   Callback interface to handle the result or errors.
     */
    public void getLocationsByCategoryPaginated(String categoryId, String searchQuery, int pageSize, int currentPage, LocationCallback callback) {
        Query query = locationCollection;

        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("category_id", categoryId);
        }

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Location> allLocations = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Location location = document.toObject(Location.class);
                        allLocations.add(location);
                    } catch (Exception e) {
                        Log.e("LocationsRepository", "Erro ao processar localizações", e);
                    }
                }

                List<Location> filteredLocations = new ArrayList<>();
                for (Location location : allLocations) {
                    if (location.getName() != null && location.getNameEn() != null) {
                        if (searchQuery == null || searchQuery.trim().isEmpty() ||
                                location.getName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                location.getNameEn().toLowerCase().contains(searchQuery.toLowerCase())) {
                            filteredLocations.add(location);
                        }
                    }
                }

                filteredLocations.sort(Comparator.comparing(Location::getName));

                int start = (currentPage - 1) * pageSize;
                int end = Math.min(start + pageSize, filteredLocations.size());

                List<Location> paginatedLocations = filteredLocations.subList(start, end);

                callback.onSuccess(paginatedLocations);
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

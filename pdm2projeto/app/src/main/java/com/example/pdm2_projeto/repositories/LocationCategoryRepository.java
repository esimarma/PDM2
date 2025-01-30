package com.example.pdm2_projeto.repositories;

import android.util.Log;

import com.example.pdm2_projeto.models.LocationCategory;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LocationCategoryRepository {

    private final CollectionReference categoryCollection;

    public interface CategoryCallback {
        void onSuccess(List<LocationCategory> categories);
        void onFailure(Exception e);
    }

    public LocationCategoryRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        categoryCollection = db.collection("location_category"); // Nome da coleção no Firestore
    }

    public void getCategories(CategoryCallback callback) {
        categoryCollection.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<LocationCategory> categories = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String id = document.getId();
                                String name = document.getString("description");
                                if (name != null) {
                                    categories.add(new LocationCategory(id, name));
                                } else {
                                    Log.w("LocationCategoryRepo", "Documento sem campo 'name': " + document.getId());
                                }
                            } catch (Exception e) {
                                Log.e("LocationCategoryRepo", "Erro ao processar categoria: " + document.getId(), e);
                            }
                        }
                        callback.onSuccess(categories);
                    } else {
                        Exception e = task.getException();
                        Log.e("LocationCategoryRepo", "Erro ao buscar categorias", e);
                        callback.onFailure(e);
                    }
                });
    }
}

package com.example.pdm2_projeto;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.FavoritesRepository;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class LocationDetailFragment extends Fragment {

    // UI components
    private TextView locationName, locationCountry;
    private ImageView locationImage, favoriteIcon;
    private Button btnOpenMap;

    // Location details
    private double latitude, longitude;
    private String name;
    private String description;
    private String imageUrl;
    private String locationId;

    // Repositories for data handling
    private LocationsRepository locationsRepository;
    private FavoritesRepository favoritesRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeUI(view);
        updateHeader();
        initializeRepositories();
        loadArguments();
        setClickListeners();
    }

    private void updateHeader() {
        TextView headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.details));
        }
        View headerLogo = requireActivity().findViewById(R.id.app_icon);
        if (headerLogo != null) {
            headerLogo.setVisibility(View.VISIBLE);
        }
    }

    // Initializes UI components by binding them to layout elements
    private void initializeUI(View view) {
        locationName = view.findViewById(R.id.location_name);
        locationCountry = view.findViewById(R.id.location_country);
        locationImage = view.findViewById(R.id.location_image);
        btnOpenMap = view.findViewById(R.id.btn_open_map);
        favoriteIcon = view.findViewById(R.id.favorite_icon);
    }

    // Initializes repositories to manage location and favorite data
    private void initializeRepositories() {
        locationsRepository = new LocationsRepository();
        favoritesRepository = new FavoritesRepository();
    }

    // Retrieves arguments passed to the fragment and fetches necessary data
    private void loadArguments() {
        Bundle args = getArguments();
        if (args != null) {
            locationId = args.getString("locationId");
            if (locationId != null) {
                fetchLocationDetails(locationId);
                checkIfLocationIsFavorited();
            }
        }
    }

    // Sets event listeners for UI components
    private void setClickListeners() {
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        btnOpenMap.setOnClickListener(v -> openMapFragment());
    }

    // Checks if the current location is marked as a favorite
    private void checkIfLocationIsFavorited() {
        if (locationId == null) return;
        favoritesRepository.isLocationFavorited(locationId)
                .addOnSuccessListener(this::updateFavoriteIcon)
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking favorite", e));
    }

    // Updates the favorite icon UI based on the favorite status
    private void updateFavoriteIcon(boolean isFavorite) {
        favoriteIcon.setImageResource(isFavorite ? R.drawable.ic_favorite_checked : R.drawable.ic_favorite_unchecked);
    }

    // Toggles favorite status for the location
    private void toggleFavorite() {
        if (locationId == null) return;
        favoritesRepository.isLocationFavorited(locationId)
                .addOnSuccessListener(isFav -> {
                    if (isFav) {
                        removeFavorite();
                    } else {
                        addFavorite();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking favorite", e));
    }

    // Removes the location from favorites
    private void removeFavorite() {
        favoritesRepository.removeFavorite(locationId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateFavoriteIcon(false);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Error removing favorite", e);
            }
        });
    }

    // Adds the location to favorites
    private void addFavorite() {
        favoritesRepository.addFavorite(locationId, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateFavoriteIcon(true);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Error adding favorite", e);
            }
        });
    }

    // Fetches location details from the repository and updates UI
    private void fetchLocationDetails(String locationId) {
        locationsRepository.getLocationById(locationId, new LocationsRepository.SingleLocationCallback() {
            @Override
            public void onSuccess(Location location) {
                updateUIWithLocationDetails(location);
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updates UI components with the retrieved location details
    private void updateUIWithLocationDetails(Location location) {
        name = location.getName();
        description = location.getDescription();
        String country = location.getCountry();
        imageUrl = location.getImageUrl();
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        locationName.setText(name);
        locationCountry.setText(country);
        Glide.with(requireContext()).load(imageUrl).into(locationImage);
    }

    // Opens a new fragment to display the location on a map
    private void openMapFragment() {
        Fragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putString("locationName", name);
        bundle.putString("locationDescription", description);
        bundle.putString("imageUrl", imageUrl);
        bundle.putDouble("latitude", latitude);
        bundle.putDouble("longitude", longitude);
        mapFragment.setArguments(bundle);

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_map);
        }

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .addToBackStack(null)
                .commit();
    }
}


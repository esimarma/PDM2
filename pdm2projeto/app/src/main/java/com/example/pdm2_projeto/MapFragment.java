package com.example.pdm2_projeto;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

/**
 * Fragment that displays a map with user location and markers from Firestore.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationsRepository locationRepository;
    private TextView headerTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize location client and repository
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationRepository = new LocationsRepository();

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            showToast("Map fragment is null.");
        }

        // Make the bottom navigation bar visible
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);

        // Make the top header visible
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);

        // Find and update the header title for this fragment
        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.map)); // Set the header title for this fragment
        }

        // Find the settings button and set up a click listener to navigate to the SettingsFragment
        ImageView settingsButton = requireActivity().findViewById(R.id.menu_icon);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment()) // Replace current fragment with SettingsFragment
                        .addToBackStack(null) // Add the transaction to the back stack
                        .commit(); // Commit the fragment transaction
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Enable user location on the map if permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Fetch user location and add a marker
        getUserLocation();

        // Fetch locations from Firestore and add markers
        fetchLocationsFromFirestore();
    }

    private void getUserLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            // Get the last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Convert the location to LatLng and add a marker
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                    .position(userLatLng).
                                    title(getString(R.string.your_location)));
                            // Center the camera on the user's location
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12));
                        } else {
                            // Show a default location or message if location is null
                            LatLng defaultLatLng = new LatLng(0, 0);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 2));
                            showToast(getString(R.string.your_location_error));
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors while getting location
                        showToast(getString(R.string.your_location_error));
                        e.printStackTrace();
                    });
        } else {
            // Request location permission from the user
            requestLocationPermission();
        }
    }

    /**
     * Requests location permission from the user.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    /**
     * Handles the result of location permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted - get the location
            getUserLocation();
        } else {
            // Permission denied - show a message to the user
            showToast(getString(R.string.permission_denied));
        }
    }

    /**
     * Displays a Toast message to the user.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Fetches locations from Firestore and adds markers to the map.
     */
    private void fetchLocationsFromFirestore() {
        locationRepository.getAllLocations(new LocationsRepository.LocationCallback() {
            @Override
            public void onSuccess(List<Location> locations) {
                for (Location location : locations) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(location.getName())
                            .snippet(location.getDescription())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Handle errors (e.g., log them or show a message to the user)
                showToast(getString(R.string.failed_to_fetch_locations));
                e.printStackTrace();
            }
        });
    }
}
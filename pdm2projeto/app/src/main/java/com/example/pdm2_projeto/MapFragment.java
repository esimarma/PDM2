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

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;

/**
 * Fragment that displays a map with user location and markers from Firestore.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationsRepository locationRepository;
    private TextView headerTitle;
    private final HashMap<Marker, LocationInfo> markerData = new HashMap<>();

    static class LocationInfo {
        String title;
        String description;
        String imageUrl;

        LocationInfo(String title, String description, String imageUrl) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        locationRepository = new LocationsRepository();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            showToast("Map fragment is null.");
        }

        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);

        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.map));
        }

        ImageView settingsButton = requireActivity().findViewById(R.id.menu_icon);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Verifica se há argumentos (localização clicada)
        Bundle args = getArguments();
        if (args != null) {
            String locationName = args.getString("locationName");
            String locationDescription = args.getString("locationDescription");
            String imageUrl = args.getString("imageUrl");
            double latitude = args.getDouble("latitude");
            double longitude = args.getDouble("longitude");

            LatLng locationLatLng = new LatLng(latitude, longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(locationLatLng)
                    .title(locationName)
                    .snippet(locationDescription)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            if (marker != null) {
                marker.showInfoWindow();
            }

            // Move a câmera para a localização especificada
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15));
        } else {
            // Caso contrário, usa a localização do utilizador
            getUserLocation();
        }

        // Carrega outras localizações, como as do Firestore
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
                                    .position(userLatLng)
                                    .title(getString(R.string.your_location))); // Use default marker

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

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            showToast(getString(R.string.permission_denied));
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void fetchLocationsFromFirestore() {
        locationRepository.getAllLocations(new LocationsRepository.LocationCallback() {
            @Override
            public void onSuccess(List<Location> locations) {
                for (Location location : locations) {
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(location.getName())
                            .snippet(location.getDescription())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                    markerData.put(marker, new LocationInfo(location.getName(), location.getDescription(), location.getImageUrl()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                showToast(getString(R.string.failed_to_fetch_locations));
                e.printStackTrace();
            }
        });
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = LayoutInflater.from(getContext()).inflate(R.layout.custom_info_window, null);
        }

        private void renderWindowText(Marker marker, View view) {
            LocationInfo locationInfo = markerData.get(marker);
            if (locationInfo == null) return;

            TextView title = view.findViewById(R.id.info_window_title);
            title.setText(locationInfo.title);

            TextView description = view.findViewById(R.id.info_window_description);
            description.setText(locationInfo.description);

            ImageView image = view.findViewById(R.id.info_window_image);
            Glide.with(getContext()).load(locationInfo.imageUrl).into(image);
        }

        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            renderWindowText(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null;
        }
    }
}
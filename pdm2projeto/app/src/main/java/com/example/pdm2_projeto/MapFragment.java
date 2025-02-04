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
    private final HashMap<Marker, LocationInfo> markerData = new HashMap<>();

    // Inner class to store location information
    static class LocationInfo {
        String title;
        String address;
        String imageUrl;

        LocationInfo(String title, String address, String imageUrl) {
            this.title = title;
            this.address = address;
            this.imageUrl = imageUrl;
        }
    }

    /*
    * Called when the view is created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    /*
    * Called when the view is created.
     */
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

        updateHeader();

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

    /**
     * Updates the header title to display "Profile".
     */
    private void updateHeader() {
        TextView headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.map));
        }
        View headerLogo = requireActivity().findViewById(R.id.app_icon);
        if (headerLogo != null) {
            headerLogo.setVisibility(View.VISIBLE);
        }
    }

    /**
    * Callback method called when the map is ready.
    * @param googleMap The GoogleMap instance.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Check if there are arguments (clicked location)
        Bundle bundle = getArguments();
        if (bundle != null) {
            String locationName = bundle.getString("locationName");
            String address = bundle.getString("address");
            String imageUrl = bundle.getString("imageUrl");
            double latitude = bundle.getDouble("latitude");
            double longitude = bundle.getDouble("longitude");

            LatLng locationLatLng = new LatLng(latitude, longitude);
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(locationLatLng)
                    .title(locationName)
                    .snippet(address)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

            if (marker != null) {
                markerData.put(marker, new LocationInfo(locationName, address, imageUrl)); // Ensure markerData contains the clicked marker
            }

            // Move the camera to the specified location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15));
            marker.showInfoWindow();
        } else {
            // Otherwise, use the user's location
            getUserLocation();
        }

        // Load other locations, such as those from Firestore
        fetchLocationsFromFirestore();
    }

    /*
    * Fetches locations from Firestore and adds them to the map.
     */
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

    /*
    * Requests location permission from the user.
    */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
    }

    /*
    * Handles the result of the location permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            showToast(getString(R.string.permission_denied));
        }
    }

    /*
    * Displays a toast message.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /*
    * Fetches locations from Firestore and adds them to the map.
     */
    private void fetchLocationsFromFirestore() {
        locationRepository.getAllLocations(new LocationsRepository.LocationCallback() {
            @Override
            public void onSuccess(List<Location> locations) {
                for (Location location : locations) {

                    String name = "";
                    String country = "";

                    if(getContext().getString(R.string.language).equals("en")){
                        name = location.getNameEn();
                        country = location.getCountryEn();
                    } else {
                        name = location.getName();
                        country = location.getCountry();
                    }
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position)
                            .title(name)
                            .snippet(country)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                    // Ensure marker is not null before adding to markerData
                    if (marker != null) {
                        markerData.put(marker, new LocationInfo(name, country, location.getImageUrl()));
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                showToast(getString(R.string.failed_to_fetch_locations));
                e.printStackTrace();
            }
        });
    }


    /*
    * Handles the click event on a marker.
     */
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

            TextView description = view.findViewById(R.id.location_description);
            description.setText(locationInfo.address);

            ImageView image = view.findViewById(R.id.info_window_image);

            // Load image asynchronously using Glide
            Glide.with(getContext())
                    .load(locationInfo.imageUrl)
                    .placeholder(R.drawable.default_placeholder) // Show a placeholder while loading
                    .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull android.graphics.drawable.Drawable resource, @Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.drawable.Drawable> transition) {
                            image.setImageDrawable(resource);

                            // Force update the InfoWindow once the image is ready
                            if (marker.isInfoWindowShown()) {
                                marker.hideInfoWindow();  // Hide first
                                marker.showInfoWindow();  // Show again to refresh
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                            image.setImageDrawable(placeholder);
                        }
                    });
        }

        /**
        * Returns the custom InfoWindow view for the given marker.
        * @param marker The marker for which to get the InfoWindow view.
         */
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            // Prevents showing an empty info window for the user’s location
            if (!markerData.containsKey(marker)) {
                return null;
            }

            renderWindowText(marker, mWindow);
            return mWindow;
        }

        /*
        * Returns null since we're using a custom InfoWindow view.
         */
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            return null;
        }
    }

}
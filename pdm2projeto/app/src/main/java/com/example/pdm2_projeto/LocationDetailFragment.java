package com.example.pdm2_projeto;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.adapters.CommentAdapter;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Comment;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.CommentsRepository;
import com.example.pdm2_projeto.repositories.FavoritesRepository;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LocationDetailFragment extends Fragment {

    // UI components
    private TextView locationName, locationCountry;
    private ImageView locationImage, favoriteIcon, btnAddComment;
    private Button btnOpenMap, btnSubmitComment;
    private EditText editComment;
    private LinearLayout commentInputContainer;
    private RecyclerView recyclerComments;

    // Location details
    private double latitude, longitude;
    private String name;
    private String address;
    private String country;
    private String imageUrl;
    private String locationId;

    // Repositories for data handling
    private LocationsRepository locationsRepository;
    private FavoritesRepository favoritesRepository;
    private CommentsRepository commentsRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        initializeUI(view);
        initializeRepositories();

        updateHeader();
        loadArguments();
        setClickListeners();

        // Initialize RecyclerView for comments
        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load comments from Firestore
        loadCommentsFromFirestore();
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
        btnAddComment = view.findViewById(R.id.btn_add_comment);
        btnSubmitComment = view.findViewById(R.id.btn_submit_comment);
        editComment = view.findViewById(R.id.edit_comment);
        commentInputContainer = view.findViewById(R.id.comment_input_container);
        recyclerComments = view.findViewById(R.id.recycler_comments);
    }

    // Initializes repositories to manage location, favorites, and comments
    private void initializeRepositories() {
        locationsRepository = new LocationsRepository();
        favoritesRepository = new FavoritesRepository();
        commentsRepository = new CommentsRepository();
    }

    // Retrieves arguments passed to the fragment and fetches necessary data
    private void loadArguments() {
        Bundle args = getArguments();
        if (args != null) {
            locationId = args.getString("locationId");
            Log.d("LocationDetailFragment", "Loaded locationId: " + locationId); // Debug Log
            if (locationId != null) {
                fetchLocationDetails(locationId);
                checkIfLocationIsFavorited();
                loadCommentsFromFirestore(); // Ensure comments are loaded when locationId is set
            }
        }
    }

    // Sets event listeners for UI components
    private void setClickListeners() {
        favoriteIcon.setOnClickListener(v -> toggleFavorite());
        btnOpenMap.setOnClickListener(v -> openMapFragment());

        btnAddComment.setOnClickListener(v -> {
            // Toggle visibility for comment input
            if (commentInputContainer.getVisibility() == View.GONE) {
                commentInputContainer.setVisibility(View.VISIBLE);
            } else {
                commentInputContainer.setVisibility(View.GONE);
            }
        });

        btnSubmitComment.setOnClickListener(v -> {
            String commentText = editComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addCommentToFirestore(commentText);
                editComment.setText(""); // Clear input field after submission
                commentInputContainer.setVisibility(View.GONE); // Hide input box
            }
        });
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

    // Save comment to Firestore
    private void addCommentToFirestore(String commentText) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Comment newComment = new Comment(userId, locationId, commentText, Timestamp.now());

        commentsRepository.addComment(newComment, new FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("Comments", "Comment added successfully!");
                new android.os.Handler().postDelayed(() -> loadCommentsFromFirestore(), 1000); // Wait 1 sec before reloading
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Error adding comment", e);
            }
        });
    }

    // Load comments from Firestore
    private void loadCommentsFromFirestore() {
        commentsRepository.getCommentsByLocation(locationId, new FirestoreCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> comments) {
                recyclerComments.setAdapter(new CommentAdapter(getContext(), comments));
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("Firestore", "Error fetching comments", e);
            }
        });
    }

    // Fetches location details and updates UI
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

    // Updates UI components with retrieved location details
    private void updateUIWithLocationDetails(Location location) {
        address = location.getAddress();

        if(getContext().getString(R.string.language).equals("en")){
            name = location.getNameEn();
            country = location.getCountryEn();
        } else {
            name = location.getName();
            country = location.getCountry();
        }

        imageUrl = location.getImageUrl();

        // Make sure these values are correctly retrieved
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        locationName.setText(name);
        locationCountry.setText(country);

        // Ensure image loads properly
        Glide.with(requireContext()).load(imageUrl).into(locationImage);
    }

    private void openMapFragment() {
        Fragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();

        // Pass location details to the map fragment
        bundle.putString("locationName", name);
        bundle.putString("address", address);
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

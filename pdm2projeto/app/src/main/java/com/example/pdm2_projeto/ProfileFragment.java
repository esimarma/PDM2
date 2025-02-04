package com.example.pdm2_projeto;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.adapters.LocationAdapter;
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Favorite;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.FavoritesRepository;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private View loggedOutContainer;
    private View loggedInContainer;
    private TextView welcomeText;
    private UsersRepository usersRepository;
    private FavoritesRepository favoritesRepository;
    private LocationsRepository locationsRepository;
    private List<Location> favoriteLocations;
    private LocationAdapter locationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views and UI components
        initializeViews(view);

        // Set up the user repository
        usersRepository = new UsersRepository();
        favoritesRepository = new FavoritesRepository();
        locationsRepository = new LocationsRepository();
        favoriteLocations = new ArrayList<>();

        RecyclerView recyclerView = view.findViewById(R.id.favorites_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        locationAdapter = new LocationAdapter(getContext(), favoriteLocations, this::openDetailFragment);
        recyclerView.setAdapter(locationAdapter);

        // Check user authentication state
        checkUserAuthentication();

        loadFavorites();

        return view;
    }

    /**
     * Initializes views and sets up click listeners for buttons and navigation.
     * @param view The root view of the fragment.
     */
    private void initializeViews(View view) {
        loggedOutContainer = view.findViewById(R.id.logged_out_container);
        loggedInContainer = view.findViewById(R.id.logged_in_container);
        welcomeText = view.findViewById(R.id.welcome_text);

        // Ensure bottom navigation and top header are visible
        setNavigationAndHeaderVisibility();

        // Update the header title
        updateHeader();

        // Set up navigation for settings button
        usersRepository = new UsersRepository();

        // Set up navigation for login and register actions
        setupAuthenticationNavigation(view);

        // Find the profile logo and add click listener
        ImageView profileLogo = view.findViewById(R.id.profile_logo);
        profileLogo.setOnClickListener(v -> navigateToFragment(new AccountFragment()));

    }

    /*
    * Load user favorites from Firestore and update the UI
     */
    private void loadFavorites() {
        favoritesRepository.getUserFavorites(new FavoritesRepository.FavoritesCallback() {
            @Override
            public void onSuccess(List<Favorite> favorites) {
                favoriteLocations.clear();
                for (Favorite favorite : favorites) {
                    locationsRepository.getLocationById(favorite.getLocationId(), new LocationsRepository.SingleLocationCallback() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(Location location) {
                            favoriteLocations.add(location);
                            locationAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
    * Open the location detail fragment for the given location
    * @param location The location to open the detail fragment for
     */
    private void openDetailFragment(Location location) {
        Fragment detailFragment = new LocationDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("locationId", location.getId());
        detailFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }


    /**
     * Ensures the bottom navigation bar and top header are visible.
     */
    private void setNavigationAndHeaderVisibility() {
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);
    }

    /**
     * Updates the header title to display "Profile".
     */
    private void updateHeader() {
        TextView headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.profile));
        }
        View headerLogo = requireActivity().findViewById(R.id.app_icon);
        if (headerLogo != null) {
            headerLogo.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up click listeners for login and register navigation.
     */
    private void setupAuthenticationNavigation(View view) {
        Button loginButton = view.findViewById(R.id.button_login);
        TextView registerText = view.findViewById(R.id.register_text);

        loginButton.setOnClickListener(v -> navigateToFragment(new LoginFragment()));
        registerText.setOnClickListener(v -> navigateToFragment(new RegisterFragment()));
    }

    /**
     * Navigates to the specified fragment.
     */
    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }


    /**
     * Checks if a user is authenticated and handles the UI state accordingly.
     */
    private void checkUserAuthentication() {
        FirebaseUser currentUser = usersRepository.getAuthenticatedUser();

        if (currentUser != null) {
            handleLoggedInState(currentUser);
        } else {
            handleLoggedOutState();
        }
    }

    /**
     * Handles the UI and data fetching for a logged-in user.
     */
    private void handleLoggedInState(FirebaseUser currentUser) {
        toggleVisibility(true);

        usersRepository.getCurrentUser(new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
                String name = user.getName();
                welcomeText.setText(getString(R.string.hello) + " " + (name != null ? name : getString(R.string.user)));

                // Find the profile image view
                ImageView profileImage = requireView().findViewById(R.id.profile_logo);

                // Load the profile picture using Glide
                if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                    Glide.with(requireContext())
                            .load(user.getProfilePictureUrl())
                            .placeholder(R.drawable.ic_profile) // Default profile picture
                            .into(profileImage);
                }

                // Set up click listener for navigating to AccountFragment
                profileImage.setOnClickListener(v -> navigateToFragment(new AccountFragment()));
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ProfileFragment", "Error fetching user data: " + e.getMessage());
                welcomeText.setText(getString(R.string.hello) + " " + getString(R.string.user));
            }
        });
    }

    /**
     * Handles the UI state for a logged-out user.
     */
    private void handleLoggedOutState() {
        toggleVisibility(false);
    }

    /**
     * Toggles visibility of the logged-in and logged-out containers.
     */
    private void toggleVisibility(boolean isLoggedIn) {
        loggedOutContainer.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        loggedInContainer.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
    }

    /*
    * Ensures the top header is visible when this fragment is active.
     */
    @Override
    public void onResume() {
        super.onResume();
        setNavigationAndHeaderVisibility();
    }
}
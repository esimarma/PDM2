package com.example.pdm2_projeto;

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

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private View loggedOutContainer, loggedInContainer, headerLogo;
    private TextView welcomeText, headerTitle;
    private UsersRepository usersRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views and UI components
        initializeViews(view);

        // Set up the user repository
        usersRepository = new UsersRepository();

        // Check user authentication state
        checkUserAuthentication();

        return view;
    }

    /**
     * Initializes views and sets up click listeners for buttons and navigation.
     */
    private void initializeViews(View view) {
        loggedOutContainer = view.findViewById(R.id.logged_out_container);
        loggedInContainer = view.findViewById(R.id.logged_in_container);
        welcomeText = view.findViewById(R.id.welcome_text);

        // Ensure bottom navigation and top header are visible
        setNavigationAndHeaderVisibility(View.VISIBLE);

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

    /**
     * Ensures the bottom navigation bar and top header are visible.
     */
    private void setNavigationAndHeaderVisibility(int visibility) {
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(visibility);
        requireActivity().findViewById(R.id.top_header).setVisibility(visibility);
    }

    /**
     * Updates the header title to display "Profile".
     */
    private void updateHeader() {
        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.profile));
        }
        headerLogo = requireActivity().findViewById(R.id.app_icon);
        if (headerLogo != null) {
            headerLogo.setVisibility(View.GONE);
        }
    }

    /**
     * Sets up the settings button to navigate to the SettingsFragment.
     */
    private void setupSettingsButton() {
        ImageView settingsButton = requireActivity().findViewById(R.id.menu_icon);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> navigateToFragment(new SettingsFragment()));
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

                // Set up click listener for profile image when logged in
                ImageView profileImage = requireView().findViewById(R.id.profile_logo);
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

    @Override
    public void onResume() {
        super.onResume();
        // Ensure the top header is visible when this fragment is active
        setNavigationAndHeaderVisibility(View.VISIBLE);
    }
}
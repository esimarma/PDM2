package com.example.pdm2_projeto;

import android.content.Intent;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

/**
 * Fragment responsible for the user profile.
 */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize the user repository and check the authenticated user
        UsersRepository usersRepository = new UsersRepository();
        FirebaseUser currentUser = usersRepository.getAuthenticatedUser();

        // Bind UI elements to variables
        View loggedOutContainer = view.findViewById(R.id.logged_out_container);
        View loggedInContainer = view.findViewById(R.id.logged_in_container);
        View topHeader = view.findViewById(R.id.top_header);
        TextView welcomeText = view.findViewById(R.id.welcome_text);
        Button loginButton = view.findViewById(R.id.button_login);
        TextView registerText = view.findViewById(R.id.register_text);
        ImageView settingsButton = view.findViewById(R.id.menu_icon);

        if (currentUser != null) {
            // Authenticated user: show the logged-in container and hide the logged-out one
            loggedOutContainer.setVisibility(View.GONE);
            loggedInContainer.setVisibility(View.VISIBLE);
            topHeader.setVisibility(View.VISIBLE);

            // Fetch user information from Firestore
            usersRepository.getCurrentUser(new FirestoreCallback() {
                @Override
                public void onSuccess(Object result) {
                    User user = (User) result; // Convert the result to the User model
                    String name = user.getName(); // Get the user's name
                    welcomeText.setText("Hello " + (name != null ? name : "User")); // Personalized greeting
                }

                @Override
                public void onFailure(Exception e) {
                    // Log the error and display a generic message
                    Log.e("ProfileFragment", "Error fetching user data: " + e.getMessage());
                    welcomeText.setText("Hello User");
                }
            });

            // Configure the settings button
            settingsButton.setOnClickListener(v -> {
                // Redirect to the settings screen
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            });

        } else {
            // Unauthenticated user: show the logged-out container and hide the logged-in one
            loggedOutContainer.setVisibility(View.VISIBLE);
            loggedInContainer.setVisibility(View.GONE);
            topHeader.setVisibility(View.GONE);

            // Configure the login button
            loginButton.setOnClickListener(v -> {
                // Redirect to the login screen
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            });

            // Configure the register text
            registerText.setOnClickListener(v -> {
                // Redirect to the registration screen
                Intent intent = new Intent(getContext(), RegisterActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}
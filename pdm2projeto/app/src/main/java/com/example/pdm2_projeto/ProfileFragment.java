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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Find views
        View loggedOutContainer = view.findViewById(R.id.logged_out_container);
        View loggedInContainer = view.findViewById(R.id.logged_in_container);
        View topHeader = view.findViewById(R.id.top_header);
        TextView welcomeText = view.findViewById(R.id.welcome_text);
        Button loginButton = view.findViewById(R.id.button_login);
        TextView registerText = view.findViewById(R.id.register_text);
        ImageView settingsButton = view.findViewById(R.id.menu_icon); // Find the settings button

        if (currentUser != null) {
            loggedOutContainer.setVisibility(View.GONE);
            loggedInContainer.setVisibility(View.VISIBLE);
            topHeader.setVisibility(View.VISIBLE);

            // Fetch user's name from Firestore
            String userId = currentUser.getUid();
            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            welcomeText.setText("Olá " + (name != null ? name : "Utilizador"));
                        } else {
                            Log.e("ProfileFragment", "Document does not exist");
                            welcomeText.setText("Olá Utilizador");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProfileFragment", "Failed to fetch user data: " + e.getMessage());
                        welcomeText.setText("Olá Utilizador");
                    });

            // Settings button functionality
            settingsButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            });

        } else {
            loggedOutContainer.setVisibility(View.VISIBLE);
            loggedInContainer.setVisibility(View.GONE);
            topHeader.setVisibility(View.GONE);

            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            });

            registerText.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), RegisterActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }
}

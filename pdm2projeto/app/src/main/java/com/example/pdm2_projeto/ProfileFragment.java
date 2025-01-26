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

    private View loggedOutContainer, loggedInContainer;
    private TextView welcomeText, headerTitle;
    private UsersRepository usersRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        initializeViews(view);

        // Initialize User Repository
        usersRepository = new UsersRepository();
        FirebaseUser currentUser = usersRepository.getAuthenticatedUser();

        // Handle logged-in or logged-out state
        if (currentUser != null) {
            handleLoggedInState(currentUser);
        } else {
            handleLoggedOutState();
        }

        return view;
    }

    private void initializeViews(View view) {
        loggedOutContainer = view.findViewById(R.id.logged_out_container);
        loggedInContainer = view.findViewById(R.id.logged_in_container);
        welcomeText = view.findViewById(R.id.welcome_text);

        // Reference to the top header title (from activity_main.xml)
        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText("Perfil"); // Update the header title for this fragment
        }

        Button loginButton = view.findViewById(R.id.button_login);
        TextView registerText = view.findViewById(R.id.register_text);
        ImageView settingsButton = requireActivity().findViewById(R.id.menu_icon);

        // Navigate to LoginFragment
        loginButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Navigate to RegisterFragment
        registerText.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Navigate to SettingsFragment
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

    private void handleLoggedInState(FirebaseUser currentUser) {
        toggleVisibility(true);

        usersRepository.getCurrentUser(new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
                String name = user.getName();
                welcomeText.setText("Hello " + (name != null ? name : "User"));
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("ProfileFragment", "Error fetching user data: " + e.getMessage());
                welcomeText.setText("Hello User");
            }
        });
    }

    private void handleLoggedOutState() {
        toggleVisibility(false);
    }

    private void toggleVisibility(boolean isLoggedIn) {
        loggedOutContainer.setVisibility(isLoggedIn ? View.GONE : View.VISIBLE);
        loggedInContainer.setVisibility(isLoggedIn ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure the top header is visible when ProfileFragment is active
        View topHeader = requireActivity().findViewById(R.id.top_header);
        if (topHeader != null) {
            topHeader.setVisibility(View.VISIBLE);
        }
    }
}

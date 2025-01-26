package com.example.pdm2_projeto;

import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment responsible for user login.
 */
public class LoginFragment extends Fragment {

    private FirebaseAuth auth; // Firebase authentication
    private UsersRepository usersRepository; // Firestore data repository
    private Button loginButton;
    private EditText emailField, passwordField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize FirebaseAuth and the user repository
        auth = FirebaseAuth.getInstance();
        usersRepository = new UsersRepository();

        // Bind UI elements to variables
        emailField = view.findViewById(R.id.email_field);
        passwordField = view.findViewById(R.id.password_field);
        loginButton = view.findViewById(R.id.login_button);
        CheckBox showPasswordCheckbox = view.findViewById(R.id.show_password);
        ImageView backButton = view.findViewById(R.id.back_button);
        TextView registerButton = view.findViewById(R.id.button_register);

        // Hide the top header for the LoginFragment
        hideTopHeader();

        // Configure the back button
        configureBackButton(backButton);

        // Configure the show/hide password functionality
        configureShowPasswordCheckbox(showPasswordCheckbox);

        // Configure the login button
        configureLoginButton();

        // Register button opens the RegisterFragment
        configureRegisterButton(registerButton);

        return view;
    }

    private void hideTopHeader() {
        // Hide the top header for LoginFragment
        View topHeader = requireActivity().findViewById(R.id.top_header);
        if (topHeader != null) {
            topHeader.setVisibility(View.GONE);
        }
    }

    private void configureBackButton(ImageView backButton) {
        backButton.setOnClickListener(v -> {
            Fragment profileFragment = new ProfileFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .commit();
        });
    }

    private void configureShowPasswordCheckbox(CheckBox showPasswordCheckbox) {
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            passwordField.setInputType(isChecked
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordField.setSelection(passwordField.getText().length());
        });
    }

    private void configureLoginButton() {
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (!validateInputs(email, password)) return;

            loginButton.setEnabled(false); // Disable button to prevent multiple requests
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> fetchUserData())
                    .addOnFailureListener(e -> {
                        showToast(getString(R.string.login_failed) + e.getMessage());
                        loginButton.setEnabled(true);
                    });
        });
    }

    private void configureRegisterButton(TextView registerButton) {
        registerButton.setOnClickListener(v -> {
            Fragment registerFragment = new RegisterFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, registerFragment) // Adjust container ID as necessary
                    .addToBackStack(null)
                    .commit();
        });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            showToast(getString(R.string.invalid_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(getString(R.string.invalid_email));
            return false;
        }
        if (password.isEmpty()) {
            showToast(getString(R.string.password_length));
            return false;
        }
        if (password.length() < 6) {
            showToast(getString(R.string.password_length));
            return false;
        }
        return true;
    }

    /**
     * Fetches user data from Firestore after successful login.
     */
    private void fetchUserData() {
        usersRepository.getCurrentUser(new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result;
                showToast(getString(R.string.welcome) + ", " + user.getName());

                // Replace LoginFragment with ProfileFragment after successful login
                Fragment profileFragment = new ProfileFragment();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, profileFragment) // Adjust container ID as necessary
                        .commit();
            }

            @Override
            public void onFailure(Exception e) {
                showToast("Failed to fetch user data: " + e.getMessage());
                loginButton.setEnabled(true);
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideTopHeader(); // Ensure the header is hidden every time the fragment is visible
    }

    @Override
    public void onPause() {
        super.onPause();
        // Show the top header again when leaving the LoginFragment
        View topHeader = requireActivity().findViewById(R.id.top_header);
        if (topHeader != null) {
            topHeader.setVisibility(View.VISIBLE);
        }
    }
}


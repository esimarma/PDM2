package com.example.pdm2_projeto;

import android.os.Bundle;
import android.text.InputType;
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
import com.google.firebase.Timestamp;

import java.util.Objects;

/**
 * Fragment responsible for registering new users.
 */
public class RegisterFragment extends Fragment {

    private UsersRepository usersRepository;

    /**
     * Inflates the layout and initializes UI components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Hide the top header specific to this fragment
        hideHeaderAndFooter();

        // Initialize the users repository
        usersRepository = new UsersRepository();

        // Bind UI elements
        EditText nameField = view.findViewById(R.id.name_field);
        EditText emailField = view.findViewById(R.id.email_field);
        EditText passwordField = view.findViewById(R.id.password_field);
        EditText confirmPasswordField = view.findViewById(R.id.confirm_password_field);
        CheckBox showPassword = view.findViewById(R.id.show_password);
        Button registerButton = view.findViewById(R.id.register_button);
        ImageView backButton = view.findViewById(R.id.back_button);
        TextView loginText = view.findViewById(R.id.login_text);

        // Set up event listeners
        configureBackButton(backButton);
        configureShowPasswordCheckbox(showPassword, passwordField, confirmPasswordField);
        configureRegisterButton(registerButton, nameField, emailField, passwordField, confirmPasswordField);
        configureLoginText(loginText);

        return view;
    }

    /**
     * Hides the header and footer elements of the UI.
     */
    private void hideHeaderAndFooter() {
        View topHeader = requireActivity().findViewById(R.id.top_header);
        if (topHeader != null) {
            topHeader.setVisibility(View.GONE);
        }
        View bottomFooter = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomFooter != null) {
            bottomFooter.setVisibility(View.GONE);
        }
    }

    /**
     * Configures the back button to navigate to the previous fragment.
     * @param backButton ImageView representing the back button.
     */
    private void configureBackButton(ImageView backButton) {
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * Configures the "Show Password" checkbox to toggle password visibility.
     * @param showPassword Checkbox to show or hide password input.
     * @param passwordField EditText for password input.
     * @param confirmPasswordField EditText for confirming the password.
     */
    private void configureShowPasswordCheckbox(CheckBox showPassword, EditText passwordField, EditText confirmPasswordField) {
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;

            passwordField.setInputType(inputType);
            confirmPasswordField.setInputType(inputType);

            // Maintain cursor position
            passwordField.setSelection(passwordField.getText().length());
            confirmPasswordField.setSelection(confirmPasswordField.getText().length());
        });
    }

    /**
     * Configures the register button to handle user registration.
     * @param registerButton Button to initiate registration.
     * @param nameField EditText for user's name.
     * @param emailField EditText for user's email.
     * @param passwordField EditText for password input.
     * @param confirmPasswordField EditText for confirming the password.
     */
    private void configureRegisterButton(Button registerButton, EditText nameField, EditText emailField, EditText passwordField, EditText confirmPasswordField) {
        registerButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (!validateInputFields(name, email, password, confirmPassword)) {
                return;
            }

            registerUser(name, email, password);
        });
    }

    /**
     * Validates user input fields for registration.
     * @param name User's name.
     * @param email User's email.
     * @param password User's password.
     * @param confirmPassword Confirmation password.
     * @return True if input is valid, false otherwise.
     */
    private boolean validateInputFields(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match.");
            return false;
        }

        return true;
    }

    /**
     * Registers a new user using Firebase authentication.
     * @param name User's name.
     * @param email User's email.
     * @param password User's password.
     */
    private void registerUser(String name, String email, String password) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Timestamp createdAt = Timestamp.now();
                    String userId = Objects.requireNonNull(authResult.getUser()).getUid();
                    User user = new User(userId, name, email, null, createdAt);

                    usersRepository.registerUser(user, new FirestoreCallback() {
                        @Override
                        public void onSuccess(Object result) {
                            showToast("Account created successfully!");
                            navigateToLogin();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            showToast("Error saving user data: " + e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> showToast("Error creating account: " + e.getMessage()));
    }

    /**
     * Configures the login text to navigate to the login screen.
     * @param loginText TextView that navigates to login screen.
     */
    private void configureLoginText(TextView loginText) {
        loginText.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        ((MainActivity) requireActivity()).showLoginFragment();
    }

    /**
     * Displays a toast message.
     * @param message The message to be displayed.
     */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}

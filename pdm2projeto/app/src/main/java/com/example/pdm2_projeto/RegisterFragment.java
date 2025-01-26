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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

/**
 * Fragment responsible for registering new users.
 */
public class RegisterFragment extends Fragment {

    private UsersRepository usersRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize the users repository
        usersRepository = new UsersRepository();

        // Bind UI elements to variables
        EditText nameField = view.findViewById(R.id.name_field);
        EditText emailField = view.findViewById(R.id.email_field);
        EditText passwordField = view.findViewById(R.id.password_field);
        EditText confirmPasswordField = view.findViewById(R.id.confirm_password_field);
        CheckBox showPassword = view.findViewById(R.id.show_password);
        Button registerButton = view.findViewById(R.id.register_button);
        ImageView backButton = view.findViewById(R.id.back_button);
        TextView loginText = view.findViewById(R.id.login_text);

        // Back button configuration
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Show/hide password functionality
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            passwordField.setInputType(inputType);
            confirmPasswordField.setInputType(inputType);

            passwordField.setSelection(passwordField.getText().length());
            confirmPasswordField.setSelection(confirmPasswordField.getText().length());
        });

        // Register button functionality
        registerButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showToast("Please fill in all fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showToast("Passwords do not match.");
                return;
            }

            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String userId = authResult.getUser().getUid();
                        User user = new User(userId,
                                name,
                                email,
                                null,
                                String.valueOf(System.currentTimeMillis())
                        );

                        usersRepository.registerUser(user, new FirestoreCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                showToast("Account created successfully!");
                                ((MainActivity) requireActivity()).showLoginFragment();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                showToast("Error saving user data: " + e.getMessage());
                            }
                        });
                    })
                    .addOnFailureListener(e -> showToast("Error creating account: " + e.getMessage()));
        });

        // Navigate to login screen
        loginText.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).showLoginFragment();
        });

        return view;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}

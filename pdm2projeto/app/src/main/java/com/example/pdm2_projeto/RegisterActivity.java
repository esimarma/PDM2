package com.example.pdm2_projeto;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.User;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity responsible for registering new users.
 */
public class RegisterActivity extends AppCompatActivity {

    private UsersRepository usersRepository; // Repository for managing operations with Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize the users repository
        usersRepository = new UsersRepository();

        // Bind UI elements to variables
        EditText nameField = findViewById(R.id.name_field);
        EditText emailField = findViewById(R.id.email_field);
        EditText passwordField = findViewById(R.id.password_field);
        EditText confirmPasswordField = findViewById(R.id.confirm_password_field);
        CheckBox showPassword = findViewById(R.id.show_password);
        Button registerButton = findViewById(R.id.register_button);
        ImageView backButton = findViewById(R.id.back_button);
        TextView loginText = findViewById(R.id.login_text);

        // Back button configuration
        backButton.setOnClickListener(v -> finish()); // Closes the current activity and returns to the previous one

        // Configuration to show or hide the password
        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD // Show password
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD; // Hide password
            passwordField.setInputType(inputType);
            confirmPasswordField.setInputType(inputType);

            // Ensures the cursor remains at the end of the text after changing input type
            passwordField.setSelection(passwordField.getText().length());
            confirmPasswordField.setSelection(confirmPasswordField.getText().length());
        });

        // Configuration of the register button
        registerButton.setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            // Validation of mandatory fields
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation of password match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            // User creation in Firebase Authentication
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        String userId = authResult.getUser().getUid(); // Get the authenticated user's ID

                        // Create the User model
                        User user = new User(
                                userId,
                                name,
                                email,
                                null, // Profile photo URL (can be null during registration)
                                String.valueOf(System.currentTimeMillis()) // Timestamp as a String
                        );

                        // Register the user in Firestore
                        usersRepository.registerUser(user, new FirestoreCallback() {
                            @Override
                            public void onSuccess(Object result) {
                                // Display a success message
                                Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                                // Redirect to the login screen
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Display an error message when failing to save user data in Firestore
                                Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Display an error message when failing to create the account in Firebase Authentication
                        Toast.makeText(RegisterActivity.this, "Error creating account: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // Configuration to navigate to the login screen
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the registration screen
        });
    }
}
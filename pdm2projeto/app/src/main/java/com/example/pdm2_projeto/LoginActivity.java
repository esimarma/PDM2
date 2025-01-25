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
 * Activity responsible for user login.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth; // Firebase authentication
    private UsersRepository usersRepository; // Firestore data repository

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth and the user repository
        auth = FirebaseAuth.getInstance();
        usersRepository = new UsersRepository();

        // Bind UI elements to variables
        EditText emailField = findViewById(R.id.email_field);
        EditText passwordField = findViewById(R.id.password_field);
        Button loginButton = findViewById(R.id.login_button);
        CheckBox showPasswordCheckbox = findViewById(R.id.show_password);
        ImageView backButton = findViewById(R.id.back_button);
        TextView registerButton = findViewById(R.id.button_register);

        // Configure the back button
        backButton.setOnClickListener(v -> finish()); // Closes the login screen and returns to the previous one

        // Configure to show or hide the password
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int inputType = isChecked
                    ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD // Show password
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD; // Hide password
            passwordField.setInputType(inputType);

            // Ensure the cursor stays at the end of the text
            passwordField.setSelection(passwordField.getText().length());
        });

        // Configure the login button
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            // Validate required fields
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate with Firebase
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        // After successful login, fetch user information from Firestore
                        fetchUserData();
                    })
                    .addOnFailureListener(e -> {
                        // Display login error message
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // Configure the "Create account" button
        registerButton.setOnClickListener(v -> {
            // Redirect to the registration screen
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Fetches user data from Firestore after successful login.
     */
    private void fetchUserData() {
        usersRepository.getCurrentUser(new FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User user = (User) result; // Convert the result to the User model
                Toast.makeText(LoginActivity.this, "Welcome, " + user.getName(), Toast.LENGTH_SHORT).show();

                // Redirect to MainActivity passing the User object
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("user", user); // Pass the User object as an extra
                startActivity(intent);
                finish(); // Close the login screen
            }

            @Override
            public void onFailure(Exception e) {
                // Display an error message when fetching user data
                Toast.makeText(LoginActivity.this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
package com.example.pdm2_projeto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth


        // Back Button
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Option Clicks
        TextView optionLanguage = findViewById(R.id.option_language);
        TextView optionGithub = findViewById(R.id.option_github);
        TextView optionLogout = findViewById(R.id.option_logout);

        optionLanguage.setOnClickListener(v -> {
            // Handle language option
            // Example: Open a new activity for language settings
        });

        optionGithub.setOnClickListener(v -> {
            // Handle GitHub option
            // Example: Open a browser with the GitHub link
        });

        // Logout Logic
        optionLogout.setOnClickListener(v -> {
            auth.signOut(); // Sign out the user

            // Redirect to LoginActivity
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the back stack
            startActivity(intent);
            finish(); // Close SettingsActivity
        });
    }
}

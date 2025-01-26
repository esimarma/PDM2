package com.example.pdm2_projeto;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carregar idioma salvo (ou usar o idioma do sistema como padrão)
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = preferences.getString("language_preference", Locale.getDefault().getLanguage());

        // Aplicar o idioma antes de carregar o layout
        setLocale(languageCode);

        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false); // Show bottom navigation by default
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.nav_map) {
                selectedFragment = new MapFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, false);
            }
            return true;
        });
    }

    /**
     * Loads the given fragment into the container.
     *
     * @param fragment   The fragment to be loaded.
     * @param hideBottom Whether to hide the bottom navigation bar.
     */
    private void loadFragment(@NonNull Fragment fragment, boolean hideBottom) {
        if (hideBottom) {
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Add fragment to back stack for navigation
                .commit();
    }

    /**
     * Replaces the current fragment with the LoginFragment and hides the bottom navigation bar.
     */
    public void showLoginFragment() {
        loadFragment(new LoginFragment(), true); // Hide bottom navigation for LoginFragment
    }

    /**
     * Replaces the current fragment with the RegisterFragment and hides the bottom navigation bar.
     */
    public void showRegisterFragment() {
        loadFragment(new RegisterFragment(), true); // Hide bottom navigation for RegisterFragment
    }

    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}

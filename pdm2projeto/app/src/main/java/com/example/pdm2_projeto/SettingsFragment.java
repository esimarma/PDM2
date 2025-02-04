package com.example.pdm2_projeto;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdm2_projeto.adapters.SettingsAdapter;
import com.example.pdm2_projeto.repositories.UsersRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.content.res.Configuration;
import androidx.appcompat.app.AlertDialog;
import android.content.SharedPreferences;

/**
 * SettingsFragment provides an interface for managing user settings, including
 * language selection, account settings, and logout options.
 */
public class SettingsFragment extends Fragment {

    private FirebaseAuth auth; // Firebase Authentication instance for user login/logout handling
    private UsersRepository usersRepository; // Repository for user-related operations

    private static final String LANGUAGE_PREFERENCE = "language_preference"; // Key for storing language preferences in SharedPreferences

    /**
     * Inflates the settings fragment layout and initializes UI components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Load the saved language preference before rendering the UI
        loadLanguagePreference();

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        auth = FirebaseAuth.getInstance(); // Initialize Firebase authentication instance

        // Configure UI components
        configureBackButton(view);
        configureRecyclerView(view);

        // Hide header and footer navigation elements in this fragment
        hideHeaderAndFooter();

        return view;
    }

    /**
     * Hides the top header and bottom navigation bar specific to this fragment.
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
     * Configures the back button to navigate to the previous screen when clicked.
     *
     * @param view The fragment view containing the back button.
     */
    private void configureBackButton(View view) {
        view.findViewById(R.id.back_button).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * Sets up the RecyclerView to display settings options.
     *
     * @param view The fragment view containing the RecyclerView.
     */
    private void configureRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.settings_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<String> settingsOptions = createSettingsOptions(); // Generate settings menu items
        recyclerView.setAdapter(new SettingsAdapter(settingsOptions, this::handleSettingClick));
    }

    /**
     * Creates a list of available settings options, dynamically adding account-related options
     * if a user is logged in.
     *
     * @return List of setting options as strings.
     */
    private List<String> createSettingsOptions() {
        List<String> settingsOptions = new ArrayList<>();

        settingsOptions.add(getString(R.string.language_option)); // Option for changing language
        settingsOptions.add(getString(R.string.github_option)); // Option to open GitHub repository

        usersRepository = new UsersRepository();
        FirebaseUser currentUser = usersRepository.getAuthenticatedUser();

        // If user is logged in, show account settings and logout options
        if (currentUser != null) {
            settingsOptions.add(getString(R.string.account_option)); // Account settings
            settingsOptions.add(getString(R.string.logout_option)); // Logout button
        }

        return settingsOptions;
    }

    /**
     * Handles clicks on individual settings options.
     *
     * @param option The selected setting option.
     */
    private void handleSettingClick(String option) {
        if (option.equals(getString(R.string.account_option))) {
            openAccountScreen(); // Navigate to AccountFragment
        } else if (option.equals(getString(R.string.language_option))) {
            showLanguageDialog(); // Show language selection dialog
        } else if (option.equals(getString(R.string.github_option))) {
            openGitHubRepository(); // Open GitHub repository in browser
        } else if (option.equals(getString(R.string.logout_option))) {
            logoutUser(); // Log the user out of the application
        }
    }

    /**
     * Displays a dialog that allows users to change the app's language.
     * The dialog presents a list of available languages and applies the selected language when confirmed.
     */
    private void showLanguageDialog() {
        String[] languages = {
                getString(R.string.language_portuguese), // Portuguese language option
                getString(R.string.language_english) // English language option
        };

        int checkedItem = getCurrentLanguageIndex(); // Get the currently selected language index

        // Create an alert dialog for language selection
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.language_dialog_title)) // Set dialog title
                .setSingleChoiceItems(languages, checkedItem, (dialogInterface, which) -> {
                    if (which == 0) {
                        changeLanguage("pt"); // Change language to Portuguese
                    } else if (which == 1) {
                        changeLanguage("en"); // Change language to English
                    }
                    dialogInterface.dismiss(); // Close the dialog after selection
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> dialogInterface.dismiss()) // Cancel button
                .create();

        dialog.show(); // Show the dialog

        // Customize the cancel button color
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.app_primary_text));
    }

    /**
     * Retrieves the index of the current language setting.
     * This is used to preselect the current language in the language selection dialog.
     *
     * @return The index of the current language in the language options array.
     */
    private int getCurrentLanguageIndex() {
        String currentLanguage = Locale.getDefault().getLanguage(); // Get system default language
        if (currentLanguage.equals("pt")) {
            return 0; // Portuguese
        } else if (currentLanguage.equals("en")) {
            return 1; // English
        }
        return -1; // Default case (should not happen)
    }

    /**
     * Changes the app's language and stores the selected preference in SharedPreferences.
     * Also updates UI elements and restarts the fragment to apply the changes.
     *
     * @param languageCode The ISO language code (e.g., "en" for English, "pt" for Portuguese).
     */
    private void changeLanguage(String languageCode) {
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGE_PREFERENCE, languageCode); // Save the selected language
        editor.apply();

        // Update the app's locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        updateBottomNavigationLabels(); // Update navigation labels
        restartFragment(); // Restart the fragment to apply changes
    }

    /**
     * Restarts the SettingsFragment to ensure UI updates reflect the new language setting.
     */
    private void restartFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment()) // Replace current fragment
                .addToBackStack(null) // Allow going back to previous fragment
                .commit();

        requireActivity().getSupportFragmentManager().popBackStack(); // Navigate back
    }

    /**
     * Updates the labels of the BottomNavigationView to reflect the new language selection.
     * Ensures that navigation options display text in the correct language.
     */
    private void updateBottomNavigationLabels() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.nav_home).setTitle(getString(R.string.home)); // Update Home label
            menu.findItem(R.id.nav_map).setTitle(getString(R.string.map)); // Update Map label
            menu.findItem(R.id.nav_profile).setTitle(getString(R.string.profile)); // Update Profile label
        }
    }

    /**
     * Opens the AccountFragment, allowing users to manage their account details.
     */
    private void openAccountScreen() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new AccountFragment()) // Navigate to AccountFragment
                .addToBackStack(null) // Allow going back to settings
                .commit();
    }

    /**
     * Opens the GitHub repository in the user's default web browser.
     */
    private void openGitHubRepository() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/esimarma/PDM2"));
        startActivity(browserIntent); // Launch browser with the GitHub repository URL
    }

    /**
     * Logs out the current user by signing them out from Firebase authentication.
     * After logout, the app navigates to the LoginFragment.
     */
    private void logoutUser() {
        auth.signOut(); // Sign out the user
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment()) // Navigate to LoginFragment
                .commit();
    }

    /**
     * Loads the saved language preference from SharedPreferences and applies it to the app.
     * Ensures that the app starts in the preferred language every time it is opened.
     */
    private void loadLanguagePreference() {
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String languageCode = preferences.getString(LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage());

        // Apply the saved language setting
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
    }
}
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

public class SettingsFragment extends Fragment {

    private FirebaseAuth auth;
    private UsersRepository usersRepository;

    private static final String LANGUAGE_PREFERENCE = "language_preference"; // Key for shared preferences

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Load the saved language preference
        loadLanguagePreference();

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        auth = FirebaseAuth.getInstance();

        // Set up UI components
        configureBackButton(view);
        configureRecyclerView(view);

        // Hide the header and footer for this fragment
        hideHeaderAndFooter();

        return view;
    }

    /**
     * Hides the header and footer specific to this fragment.
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
     * Configures the back button to navigate to the previous screen.
     */
    private void configureBackButton(View view) {
        view.findViewById(R.id.back_button).setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    /**
     * Sets up the RecyclerView with settings options.
     */
    private void configureRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.settings_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<String> settingsOptions = createSettingsOptions();
        recyclerView.setAdapter(new SettingsAdapter(settingsOptions, this::handleSettingClick));
    }

    /**
     * Creates the list of settings options based on user authentication status.
     */
    private List<String> createSettingsOptions() {
        List<String> settingsOptions = new ArrayList<>(Arrays.asList(
                getString(R.string.language_option),
                getString(R.string.github_option)
        ));

        usersRepository = new UsersRepository();
        FirebaseUser currentUser = usersRepository.getAuthenticatedUser();

        if (currentUser != null) {
            settingsOptions.add(getString(R.string.logout_option));
        }

        return settingsOptions;
    }

    /**
     * Handles the click event for a specific setting option.
     */
    private void handleSettingClick(String option) {
        if (option.equals(getString(R.string.language_option))) {
            showLanguageDialog();
        } else if (option.equals(getString(R.string.github_option))) {
            openGitHubRepository();
        } else if (option.equals(getString(R.string.logout_option))) {
            logoutUser();
        }
    }

    /**
     * Displays the language selection dialog.
     */
    private void showLanguageDialog() {
        String[] languages = {
                getString(R.string.language_portuguese),
                getString(R.string.language_english)
        };

        int checkedItem = getCurrentLanguageIndex();

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.language_dialog_title))
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        changeLanguage("pt");
                    } else if (which == 1) {
                        changeLanguage("en");
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Gets the index of the current language for the dialog selection.
     */
    private int getCurrentLanguageIndex() {
        String currentLanguage = Locale.getDefault().getLanguage();
        if (currentLanguage.equals("pt")) {
            return 0;
        } else if (currentLanguage.equals("en")) {
            return 1;
        }
        return -1;
    }

    /**
     * Changes the app's language and saves the preference.
     */
    private void changeLanguage(String languageCode) {
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LANGUAGE_PREFERENCE, languageCode);
        editor.apply();

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

        updateBottomNavigationLabels();
        restartFragment();
    }

    /**
     * Restarts the SettingsFragment to apply language changes.
     */
    private void restartFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .addToBackStack(null)
                .commit();

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Updates the labels of the BottomNavigationView.
     */
    private void updateBottomNavigationLabels() {
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            Menu menu = bottomNavigationView.getMenu();
            menu.findItem(R.id.nav_home).setTitle(getString(R.string.home));
            menu.findItem(R.id.nav_map).setTitle(getString(R.string.map));
            menu.findItem(R.id.nav_profile).setTitle(getString(R.string.profile));
        }
    }

    /**
     * Opens the GitHub repository link in a browser.
     */
    private void openGitHubRepository() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"));
        startActivity(browserIntent);
    }

    /**
     * Logs out the user and navigates to the LoginFragment.
     */
    private void logoutUser() {
        auth.signOut();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    /**
     * Loads the saved language preference and applies it.
     */
    private void loadLanguagePreference() {
        SharedPreferences preferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String languageCode = preferences.getString(LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage());

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
    }
}

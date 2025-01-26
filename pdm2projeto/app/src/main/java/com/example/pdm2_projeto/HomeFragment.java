package com.example.pdm2_projeto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private TextView headerTitle;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Make the bottom navigation bar visible
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);

        // Make the top header visible
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);

        // Find and update the header title for this fragment
        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.home)); // Set the header title for this fragment
        }

        // Find the settings button and set up a click listener to navigate to the SettingsFragment
        ImageView settingsButton = requireActivity().findViewById(R.id.menu_icon);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment()) // Replace current fragment with SettingsFragment
                        .addToBackStack(null) // Add the transaction to the back stack
                        .commit(); // Commit the fragment transaction
            });
        }

        return inflater.inflate(R.layout.fragment_home, container, false);

    }
}

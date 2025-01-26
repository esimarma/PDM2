package com.example.pdm2_projeto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends Fragment {

    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        auth = FirebaseAuth.getInstance();

        // Back button logic
        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // RecyclerView setup
        RecyclerView recyclerView = view.findViewById(R.id.settings_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Settings options
        List<String> settingsOptions = Arrays.asList("Language", "GitHub Repository", "Logout");
        recyclerView.setAdapter(new com.example.pdm2_projeto.SettingsAdapter(settingsOptions, this::handleSettingClick));

        return view;
    }

    private void handleSettingClick(String option) {
        if (option.equals("Language")) {
            // Handle language settings
            showToast("Language settings clicked");
        } else if (option.equals("GitHub Repository")) {
            // Open GitHub repository
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/"));
            startActivity(browserIntent);
        } else if (option.equals("Logout")) {
            // Sign out logic
            auth.signOut();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}

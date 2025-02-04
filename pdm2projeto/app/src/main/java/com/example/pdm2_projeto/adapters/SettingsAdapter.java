package com.example.pdm2_projeto.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdm2_projeto.R;

import java.util.List;

/**
 * Adapter class for managing the settings options in a RecyclerView.
 * Displays a list of settings options and handles click interactions.
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private final List<String> options; // List of settings options to be displayed
    private final OnItemClickListener listener; // Listener for handling item click events

    /**
     * Interface for handling click events on individual setting options.
     */
    public interface OnItemClickListener {
        void onItemClick(String option); // Called when an option is clicked
    }

    /**
     * Constructor for initializing the adapter with a list of options and a click listener.
     *
     * @param options  List of settings options.
     * @param listener Click listener for handling user interactions.
     */
    public SettingsAdapter(List<String> options, OnItemClickListener listener) {
        this.options = options;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual settings items
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings, parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        // Get the current option and set its text in the TextView
        String option = options.get(position);
        holder.textView.setText(option);

        // Set click listener for handling option selection
        holder.itemView.setOnClickListener(v -> listener.onItemClick(option));
    }

    @Override
    public int getItemCount() {
        return options.size(); // Return the number of available settings options
    }

    /**
     * ViewHolder class to hold UI components for individual settings options.
     */
    public static class SettingsViewHolder extends RecyclerView.ViewHolder {
        TextView textView; // TextView for displaying the setting option

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.setting_option_text); // Initialize UI component
        }
    }
}


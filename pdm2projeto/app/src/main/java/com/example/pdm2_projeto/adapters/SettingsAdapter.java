package com.example.pdm2_projeto.adapters;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pdm2_projeto.R;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private final List<String> options;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String option);
    }

    public SettingsAdapter(List<String> options, OnItemClickListener listener) {
        this.options = options;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_settings, parent, false);
        return new SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        String option = options.get(position);
        holder.textView.setText(option);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(option));
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public static class SettingsViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.setting_option_text);
        }
    }
}

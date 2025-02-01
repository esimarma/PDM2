package com.example.pdm2_projeto.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pdm2_projeto.R;
import com.example.pdm2_projeto.models.Location;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private Context context;
    private List<Location> locationList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    public LocationAdapter(Context context, List<Location> locationList, OnItemClickListener listener) {
        this.context = context;
        this.locationList = locationList;
        this.onItemClickListener = listener;
    }
    public void updateList(List<Location> filteredResults) {
        this.locationList.clear();  // Clear current list
        this.locationList.addAll(filteredResults); // Add new filtered results
        notifyDataSetChanged();  // Notify RecyclerView to update
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location location = locationList.get(position);
        Log.d("LocationAdapter", "Binding item: " + position + " - " + location.getName());
        holder.locationName.setText(location.getName());
        holder.locationCountry.setText(location.getCountry());

        // Carregar imagem com Glide
        Glide.with(context).load(location.getImageUrl()).into(holder.locationImage);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(location));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView locationImage, favoriteIcon;
        TextView locationName, locationCountry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationImage = itemView.findViewById(R.id.location_image);
            locationName = itemView.findViewById(R.id.location_name);
            locationCountry = itemView.findViewById(R.id.location_country);
        }
    }
}
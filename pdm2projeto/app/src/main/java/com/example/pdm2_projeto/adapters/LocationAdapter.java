package com.example.pdm2_projeto.adapters;

import static android.provider.Settings.System.getString;

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
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.FavoritesRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Adapter for displaying a list of locations in a RecyclerView.
 * Handles item clicks, favorites functionality, and image loading.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final Context context; // Context for inflating views
    private final List<Location> locationList; // List of locations to be displayed
    private final OnItemClickListener onItemClickListener; // Click listener for item selection
    private final FavoritesRepository favoritesRepository; // Repository for handling favorites
    private String userId; // ID of the logged-in user

    /**
     * Interface for handling item click events.
     */
    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    /**
     * Constructor for initializing adapter with necessary dependencies.
     *
     * @param context       Application context.
     * @param locationList  List of locations to be displayed.
     * @param listener      Click listener for handling item selections.
     */
    public LocationAdapter(Context context, List<Location> locationList, OnItemClickListener listener) {
        this.context = context;
        this.locationList = locationList;
        this.onItemClickListener = listener;
        this.favoritesRepository = new FavoritesRepository();

        // Get currently logged-in user, if any, to manage favorite locations
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.userId = (currentUser != null) ? currentUser.getUid() : null;
    }

    /**
     * Updates the location list with a filtered list and refreshes the UI.
     *
     * @param filteredResults Filtered list of locations.
     */
    public void updateList(List<Location> filteredResults) {
        this.locationList.clear();
        this.locationList.addAll(filteredResults);
        notifyDataSetChanged(); // Notify adapter about data changes
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each list item
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the location item at the current position
        Location location = locationList.get(position);

        String name;
        String country;

        // Determine language preference and set name and country accordingly
        if (context.getString(R.string.language).equals("en")) {
            name = location.getNameEn();
            country = location.getCountryEn();
        } else {
            name = location.getName();
            country = location.getCountry();
        }

        Log.d("LocationAdapter", "Binding item: " + position + " - " + name);

        // Set location name and country in the respective TextViews
        holder.locationName.setText(name);
        holder.locationCountry.setText(country);

        // Load location image using Glide
        Glide.with(context).load(location.getImageUrl()).into(holder.locationImage);

        // Manage favorite status only if the user is logged in
        if (userId != null) {
            favoritesRepository.isLocationFavorited(location.getId())
                    .addOnSuccessListener(isFav -> updateFavoriteIcon(holder.favoriteIcon, isFav))
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch favorite status", e));

            holder.favoriteIcon.setOnClickListener(v -> toggleFavorite(holder.favoriteIcon, location));
        } else {
            holder.favoriteIcon.setVisibility(View.GONE); // Hide favorite icon if user is not logged in
        }

        // Handle item click event
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(location));
    }

    @Override
    public int getItemCount() {
        return locationList.size(); // Return total number of items in the list
    }

    /**
     * ViewHolder class to hold UI components for each list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView locationImage, favoriteIcon;
        TextView locationName, locationCountry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            locationImage = itemView.findViewById(R.id.location_image);
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            locationName = itemView.findViewById(R.id.location_name);
            locationCountry = itemView.findViewById(R.id.location_country);
        }
    }

    /**
     * Toggles the favorite status of a location.
     * Adds or removes from favorites and updates the UI accordingly.
     *
     * @param favoriteIcon ImageView representing the favorite icon.
     * @param location     The location object to be toggled.
     */
    private void toggleFavorite(ImageView favoriteIcon, Location location) {
        if (userId == null) return; // Skip if user is not logged in

        favoritesRepository.isLocationFavorited(location.getId())
                .addOnSuccessListener(isFav -> {
                    if (isFav) {
                        favoritesRepository.removeFavorite(location.getId(), new FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                updateFavoriteIcon(favoriteIcon, false);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firestore", "Error removing favorite", e);
                            }
                        });
                    } else {
                        favoritesRepository.addFavorite(location.getId(), new FirestoreCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                updateFavoriteIcon(favoriteIcon, true);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("Firestore", "Error adding favorite", e);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking favorite status", e));
    }

    /**
     * Updates the favorite icon based on favorite status.
     *
     * @param icon       ImageView of the favorite icon.
     * @param isFavorite Boolean indicating whether the item is a favorite.
     */
    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.ic_favorite_checked : R.drawable.ic_favorite_unchecked);
    }
}

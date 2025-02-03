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

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final Context context;
    private final List<Location> locationList;
    private final OnItemClickListener onItemClickListener;
    private final FavoritesRepository favoritesRepository;
    private String userId;

    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    public LocationAdapter(Context context, List<Location> locationList, OnItemClickListener listener) {
        this.context = context;
        this.locationList = locationList;
        this.onItemClickListener = listener;
        this.favoritesRepository = new FavoritesRepository();

        // Prevents crash if no user is logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.userId = (currentUser != null) ? currentUser.getUid() : null;
    }

    public void updateList(List<Location> filteredResults) {
        this.locationList.clear();
        this.locationList.addAll(filteredResults);
        notifyDataSetChanged();
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

        String name = "";
        String country = "";

        if(context.getString(R.string.language).equals("en")){
            name = location.getNameEn();
            country = location.getCountryEn();
        } else {
            name = location.getName();
            country = location.getCountry();
        }

        Log.d("LocationAdapter", "Binding item: " + position + " - " + name);

        holder.locationName.setText(name);
        holder.locationCountry.setText(country);

        Glide.with(context).load(location.getImageUrl()).into(holder.locationImage);

        // Check favorites only if user is logged in
        if (userId != null) {
            favoritesRepository.isLocationFavorited(location.getId())
                    .addOnSuccessListener(isFav -> updateFavoriteIcon(holder.favoriteIcon, isFav))
                    .addOnFailureListener(e -> Log.e("Firestore", "Failed to fetch favorite status", e));

            holder.favoriteIcon.setOnClickListener(v -> toggleFavorite(holder.favoriteIcon, location));
        } else {
            holder.favoriteIcon.setVisibility(View.GONE);
        }

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
            favoriteIcon = itemView.findViewById(R.id.favorite_icon);
            locationName = itemView.findViewById(R.id.location_name);
            locationCountry = itemView.findViewById(R.id.location_country);
        }
    }

    private void toggleFavorite(ImageView favoriteIcon, Location location) {
        if (userId == null) return;

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

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        icon.setImageResource(isFavorite ? R.drawable.ic_favorite_checked : R.drawable.ic_favorite_unchecked);
    }
}

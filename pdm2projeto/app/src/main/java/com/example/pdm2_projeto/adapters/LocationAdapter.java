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
import com.example.pdm2_projeto.interfaces.FirestoreCallback;
import com.example.pdm2_projeto.models.Location;
import java.util.List;
import com.example.pdm2_projeto.repositories.FavoritesRepository;
import com.google.firebase.auth.FirebaseAuth;


public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private Context context;
    private List<Location> locationList;
    private OnItemClickListener onItemClickListener;
    private FavoritesRepository favoritesRepository;
    private String userId;

    public interface OnItemClickListener {
        void onItemClick(Location location);
    }

    public LocationAdapter(Context context, List<Location> locationList, OnItemClickListener listener) {
        this.context = context;
        this.locationList = locationList;
        this.onItemClickListener = listener;
        this.favoritesRepository = new FavoritesRepository();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
        Log.d("LocationAdapter", "Binding item: " + position + " - " + location.getName());

        holder.locationName.setText(location.getName());
        holder.locationCountry.setText(location.getCountry());

        // Carregar imagem com Glide
        Glide.with(context).load(location.getImageUrl()).into(holder.locationImage);

        // Verifica se o local já está nos favoritos
        favoritesRepository.isLocationFavorited(location.getId())
                .addOnSuccessListener(isFav -> {
            updateFavoriteIcon(holder.favoriteIcon, isFav);
        });

        // Configura o clique no ícone de favoritos
        holder.favoriteIcon.setOnClickListener(v -> toggleFavorite(holder.favoriteIcon, location));

        // Clique para abrir detalhes
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
            favoriteIcon = itemView.findViewById(R.id.favorite_icon); // Adicionado ao layout
            locationName = itemView.findViewById(R.id.location_name);
            locationCountry = itemView.findViewById(R.id.location_country);
        }
    }

    private void toggleFavorite(ImageView favoriteIcon, Location location) {
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
                                Log.e("Firestore", "Erro ao remover favorito", e);
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
                                Log.e("Firestore", "Erro ao remover favorito", e);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Erro ao verificar favorito", e));
    }

    private void updateFavoriteIcon(ImageView icon, boolean isFavorite) {
        if (isFavorite) {
            icon.setImageResource(R.drawable.ic_favorite_checked); // Ícone de favorito ativo
        } else {
            icon.setImageResource(R.drawable.ic_favorite_unchecked); // Ícone de favorito inativo
        }
    }
}
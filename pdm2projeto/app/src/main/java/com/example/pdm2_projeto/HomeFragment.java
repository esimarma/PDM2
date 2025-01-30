package com.example.pdm2_projeto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pdm2_projeto.adapters.LocationAdapter;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private LocationAdapter locationAdapter;
    private List<Location> locationList;
    private LocationsRepository locationsRepository;
    private SearchView searchView;
    TextView headerTitle;
    View headerLogo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Mostrar a Bottom Navigation e o Header
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);

        // Atualizar o título do Header
        updateHeader();

        // Configurar botão de configurações para abrir o SettingsFragment
        ImageView settingsButton = requireActivity().findViewById(R.id.menu_icon);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Configuração do SearchView e RecyclerView
        searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        locationList = new ArrayList<>();
        locationAdapter = new LocationAdapter(getContext(), locationList, this::openMapFragment);
        recyclerView.setAdapter(locationAdapter);

        locationsRepository = new LocationsRepository();

        // Carregar todas as localizações ao iniciar
        loadLocations();

        // Configurar o SearchView para pesquisar localizações
        setupSearchView();
    }

    private void updateHeader() {
        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.home));
        }
        headerLogo = requireActivity().findViewById(R.id.app_icon);
        if (headerLogo != null) {
            headerLogo.setVisibility(View.VISIBLE);
        }
    }

    private void loadLocations() {
        locationsRepository.getAllLocations(new LocationsRepository.LocationCallback() {
            @Override
            public void onSuccess(List<Location> locations) {
                locationList.clear();
                locationList.addAll(locations);
                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocations(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    loadLocations(); // Se a pesquisa for apagada, recarrega todas as localizações
                } else {
                    searchLocations(newText);
                }
                return true;
            }
        });
    }

    private void searchLocations(String query) {
        locationsRepository.getLocationsByName(query, new LocationsRepository.LocationCallback() {
            @Override
            public void onSuccess(List<Location> locations) {
                locationList.clear();
                locationList.addAll(locations);
                locationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void openMapFragment(Location location) {
        // Abrir MapFragment passando os detalhes da localização
        Fragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putString("locationName", location.getName());
        bundle.putString("locationDescription", location.getDescription());
        bundle.putString("imageUrl", location.getImageUrl());
        bundle.putDouble("latitude", location.getLatitude());
        bundle.putDouble("longitude", location.getLongitude());
        mapFragment.setArguments(bundle);

        // Atualizar o item ativo na Bottom Navigation ao mudar para o MapFragment
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_map);
        }

        // Substituir o fragmento
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
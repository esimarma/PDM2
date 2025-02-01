package com.example.pdm2_projeto;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pdm2_projeto.adapters.LocationAdapter;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.FavoritesRepository;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);

        updateHeader();

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

        searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        locationList = new ArrayList<>();
        locationAdapter = new LocationAdapter(getContext(), locationList, this::openDetailFragment);
        recyclerView.setAdapter(locationAdapter);

        locationsRepository = new LocationsRepository();

        loadLocations();
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
                    locationAdapter.updateList(locationList);
                } else {
                    searchLocations(newText);
                }
                return true;
            }
        });
    }

    private void searchLocations(String query) {
        List<Location> filteredResults = new ArrayList<>();
        for (Location location : locationList) {
            if (location.getName().toLowerCase().contains(query.toLowerCase()) ||
                    location.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredResults.add(location);
            }
        }
        locationAdapter.updateList(filteredResults);
    }

    private void openDetailFragment(Location location) {
        Fragment detailFragment = new LocationDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("locationId", location.getId());
        detailFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

}

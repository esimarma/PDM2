package com.example.pdm2_projeto;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pdm2_projeto.adapters.LocationAdapter;
import com.example.pdm2_projeto.models.Location;
import com.example.pdm2_projeto.repositories.LocationsRepository;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private LocationAdapter locationAdapter;
    private List<Location> locationList;
    private LocationsRepository locationsRepository;
    private SearchView searchView;
    private String currentFilter = null;
    TextView headerTitle;
    View headerLogo;

    private static final int PAGE_SIZE = 10;
    private boolean isLastPage = false;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationList = new ArrayList<>();
        locationsRepository = new LocationsRepository();

        setupFilters(view);

        updateMainActivity();

        setupRecyclerView(view);

        resetPagination();
        setupSearchView(view);
    }

    private void setupRecyclerView(View view) {
        locationAdapter = new LocationAdapter(getContext(), locationList, this::openDetailFragment);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(locationAdapter);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadLocations();
                    }
                }
            }
        });
    }

    private void setupFilters(View view) {
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheetDialogFragment bottomSheet = new FilterBottomSheetDialogFragment();
            bottomSheet.setFilterListener(categoryId -> {
                if (categoryId.isEmpty()) {
                    resetPagination();
                } else {
                    currentFilter = categoryId;
                    loadFilteredLocations(categoryId);
                }
            });
            bottomSheet.show(getChildFragmentManager(), "FilterBottomSheet");
        });
    }

    private void updateMainActivity() {
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.top_header).setVisibility(View.VISIBLE);

        headerTitle = requireActivity().findViewById(R.id.header_title);
        if (headerTitle != null) {
            headerTitle.setText(getString(R.string.home));
        }
        headerLogo = requireActivity().findViewById(R.id.app_icon);
        if (headerLogo != null) {
            headerLogo.setVisibility(View.VISIBLE);
        }
    }

    private void resetPagination() {
        isLastPage = false;
        isLoading = false;
        currentFilter = null;
        locationsRepository.lastDocumentSnapshot = null;
        locationList.clear();
        locationAdapter.notifyDataSetChanged();
        loadLocations();
    }

    private void loadLocations() {
        if (isLoading || isLastPage) return;

        isLoading = true;

        locationsRepository.getPaginatedLocations(PAGE_SIZE, new LocationsRepository.LocationCallback() {
            @Override
            public void onSuccess(List<Location> locations) {
                if (locations.size() < PAGE_SIZE) {
                    isLastPage = true;
                }

                locationList.addAll(locations);
                locationAdapter.notifyDataSetChanged();
                isLoading = false;
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                isLoading = false;
            }
        });
    }

    private void setupSearchView(View view) {
        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocations(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // **Don't reset the filter, just reload filtered locations**
                    if (currentFilter != null && !currentFilter.isEmpty()) {
                        loadFilteredLocations(currentFilter);
                    } else {
                        resetPagination();
                    }
                } else {
                    searchLocations(newText);
                }
                return true;
            }
        });
    }

    private void searchLocations(String query) {
        if (query == null || query.trim().isEmpty()) {
            if (currentFilter != null && !currentFilter.isEmpty()) {
                loadFilteredLocations(currentFilter); // Reload filtered locations
            } else {
                resetPagination();
            }
            return;
        }

        Log.d("SearchView", "Searching for: " + query);

        List<Location> filteredResults = new ArrayList<>();

        // **Check if a filter is active**
        if (currentFilter != null && !currentFilter.isEmpty()) {
            // Search only within the filtered list
            locationsRepository.getLocationsByCategory(currentFilter, new LocationsRepository.LocationCallback() {
                @Override
                public void onSuccess(List<Location> filteredLocations) {
                    for (Location location : filteredLocations) {
                        if (location.getName().toLowerCase().contains(query.toLowerCase()) ||
                                location.getDescription().toLowerCase().contains(query.toLowerCase())) {
                            filteredResults.add(location);
                        }
                    }

                    Log.d("SearchView", "Found " + filteredResults.size() + " results for query: " + query);
                    locationAdapter.updateList(filteredResults);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });

        } else {
            // No filter active, search in all locations
            locationsRepository.getAllLocations(new LocationsRepository.LocationCallback() {
                @Override
                public void onSuccess(List<Location> allLocations) {
                    for (Location location : allLocations) {
                        if (location.getName().toLowerCase().contains(query.toLowerCase()) ||
                                location.getDescription().toLowerCase().contains(query.toLowerCase())) {
                            filteredResults.add(location);
                        }
                    }

                    Log.d("SearchView", "Found " + filteredResults.size() + " results for query: " + query);
                    locationAdapter.updateList(filteredResults);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                }
            });
        }
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

    private void loadFilteredLocations(String categoryId) {
        locationsRepository.getLocationsByCategory(categoryId, new LocationsRepository.LocationCallback() {
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
}

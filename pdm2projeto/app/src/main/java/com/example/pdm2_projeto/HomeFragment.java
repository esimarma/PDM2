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

/**
 * HomeFragment is the main fragment displaying a list of locations.
 * It includes search functionality, filtering options, pagination,
 * and navigation to location details.
 */
public class HomeFragment extends Fragment {

    // Adapter for managing location display in RecyclerView
    private LocationAdapter locationAdapter;
    // List that holds the location data fetched from the repository
    private List<Location> locationList;
    // Repository responsible for retrieving locations from a data source
    private LocationsRepository locationsRepository;
    // SearchView widget for searching locations by name or description
    private SearchView searchView;
    // Stores the currently applied filter category
    private String currentFilter = null;

    // UI elements for the header section
    TextView headerTitle;
    View headerLogo;

    // Pagination settings to control data loading in chunks
    private static final int PAGE_SIZE = 10; // Number of locations per page
    private boolean isLastPage = false; // Flag to indicate if all pages have been loaded
    private boolean isLoading = false; // Flag to prevent multiple loads at the same time

    /**
     * Inflates the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Called after the view is created. Initializes UI components, sets up filters,
     * search view, RecyclerView, and triggers initial data loading.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize necessary components
        locationList = new ArrayList<>();
        locationsRepository = new LocationsRepository();

        setupFilters(view);
        setupSearchView(view);

        updateMainActivity();
        setupRecyclerView(view);
        resetPagination();
        loadLocations();
    }

    /**
     * Configures the RecyclerView to display locations in a grid format
     * and adds scroll listener for pagination.
     */
    private void setupRecyclerView(View view) {
        locationAdapter = new LocationAdapter(getContext(), locationList, this::openDetailFragment);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(locationAdapter);

        // Set a grid layout with 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // If we are not already loading and we haven't reached the last page
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadLocations(); // Load the next set of locations
                    }
                }
            }
        });
    }

    /**
     * Sets up the filter button to open the filter bottom sheet dialog.
     * When a filter is selected, it updates the category filter and reloads locations.
     */
    private void setupFilters(View view) {
        ImageButton btnFilter = view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            FilterBottomSheetDialogFragment bottomSheet = new FilterBottomSheetDialogFragment();
            bottomSheet.setFilterListener(categoryId -> {
                resetPagination();
                currentFilter = categoryId.isEmpty() ? null : categoryId;
                loadLocations();
            });
            bottomSheet.show(getChildFragmentManager(), "FilterBottomSheet");
        });
    }

    /**
     * Updates UI elements in the MainActivity such as the header and navigation visibility.
     */
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

    /**
     * Resets pagination state and clears location data before reloading.
     */
    private void resetPagination() {
        isLastPage = false;
        isLoading = false;
        locationsRepository.lastDocumentSnapshot = null;
        locationList.clear();
        locationAdapter.notifyDataSetChanged();
    }

    /**
     * Loads locations from the repository, either all locations or filtered by category.
     * Implements pagination to load data in chunks.
     */
    private void loadLocations() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        if (currentFilter != null) {
            locationsRepository.getLocationsByCategoryPaginated(currentFilter, PAGE_SIZE, new LocationsRepository.LocationCallback() {
                @Override
                public void onSuccess(List<Location> locations) {
                    processLocations(locations);
                }
                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    isLoading = false;
                }
            });
        } else {
            locationsRepository.getPaginatedLocations(PAGE_SIZE, new LocationsRepository.LocationCallback() {
                @Override
                public void onSuccess(List<Location> locations) {
                    processLocations(locations);
                }
                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    isLoading = false;
                }
            });
        }
    }

    /**
     * Processes loaded locations and updates the UI.
     * Determines if the last page has been reached based on the loaded data size.
     * Updates the location list and refreshes the adapter to display new data.
     *
     * @param locations List of locations retrieved from the repository.
     */
    private void processLocations(List<Location> locations) {
        if (locations.size() < PAGE_SIZE) {
            isLastPage = true;
        }
        locationList.addAll(locations); // Append new locations to the list
        locationAdapter.notifyDataSetChanged(); // Notify adapter to refresh UI
        isLoading = false; // Reset loading flag
    }

    /**
     * Sets up the search view allowing users to filter locations dynamically.
     * Listens for text changes and triggers search queries accordingly.
     *
     * @param view The root view of the fragment where the SearchView is located.
     */
    private void setupSearchView(View view) {
        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocations(query); // Trigger search when user submits query
                return true;
            }

            private String lastQuery = "";

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(lastQuery)) {
                    return true; // Ignore redundant queries
                }
                lastQuery = newText;
                if (newText.isEmpty()) {
                    resetPagination(); // Reset list if query is empty
                    loadLocations(); // Reload all locations
                } else {
                    searchLocations(newText); // Perform search with new text
                }
                return true;
            }
        });
    }

    /**
     * Filters locations based on the user-provided query.
     * Matches location names and descriptions against the search term.
     * Updates the RecyclerView with filtered results.
     *
     * @param query The search string entered by the user.
     */
    private void searchLocations(String query) {
        if (query == null || query.trim().isEmpty()) {
            resetPagination(); // Reset data when query is empty
            loadLocations(); // Load all locations
            return;
        }
        List<Location> filteredResults = new ArrayList<>();
        for (Location location : locationList) {
            if (location.getName().toLowerCase().contains(query.toLowerCase()) ||
                    location.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredResults.add(location); // Add matching locations to the list
            }
        }
        locationAdapter.updateList(filteredResults); // Update UI with filtered results
    }

    /**
     * Opens the detail fragment to display information about a selected location.
     * Passes the selected location ID to the new fragment as an argument.
     *
     * @param location The location object selected by the user.
     */
    private void openDetailFragment(Location location) {
        Fragment detailFragment = new LocationDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("locationId", location.getId()); // Pass location ID to detail fragment
        detailFragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment) // Replace current fragment
                .addToBackStack(null) // Add transaction to back stack
                .commit(); // Commit fragment transaction
    }
}


package com.example.pdm2_projeto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.pdm2_projeto.models.LocationCategory;
import com.example.pdm2_projeto.repositories.LocationCategoryRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * FilterBottomSheetDialogFragment is a modal bottom sheet that allows users to filter locations
 * by selecting a category from a dropdown list (Spinner).
 */
public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private List<LocationCategory> categories = new ArrayList<>(); // List to hold fetched categories
    private String selectedCategoryId = ""; // Stores the selected category ID
    private FilterListener filterListener; // Listener for handling filter selection events

    /**
     * Interface to communicate the selected filter back to the parent component.
     */
    public interface FilterListener {
        void onFilterSelected(String categoryId);
    }

    /**
     * Sets the filter listener for handling user selections.
     *
     * @param listener The listener to receive filter selections.
     */
    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    /**
     * Inflates the bottom sheet layout and initializes UI components.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.locations_filter, container, false);

        // Initialize UI components
        Spinner categorySpinner = view.findViewById(R.id.spinner_categories);
        Button applyButton = view.findViewById(R.id.btn_apply_filters);
        Button btnClearFilters = view.findViewById(R.id.btn_remove_filters);

        // Repository instance for fetching location categories from Firestore
        LocationCategoryRepository repository = new LocationCategoryRepository();

        // Fetch categories from Firestore and populate the Spinner
        repository.getCategories(new LocationCategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<LocationCategory> fetchedCategories) {
                categories = fetchedCategories;

                // Convert categories into a list of display names based on language preference
                List<String> categoryNames = new ArrayList<>();
                for (LocationCategory category : categories) {
                    String categoryName = "";

                    if (getContext().getString(R.string.language).equals("en")) {
                        categoryName = category.getDescriptionEn();
                    } else {
                        categoryName = category.getDescription();
                    }
                    categoryNames.add(categoryName);
                }

                // Create and set the adapter for the Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames);
                categorySpinner.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                // Display error message in case of failure
                Toast.makeText(getContext(), "Erro ao carregar categorias", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener to capture selected category from the Spinner
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categories.get(position).getId(); // Store the selected category ID
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = ""; // Reset selection if nothing is selected
            }
        });

        // Apply button to confirm the selected filter
        applyButton.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFilterSelected(selectedCategoryId); // Pass the selected category ID to listener
            }
            dismiss(); // Close the bottom sheet
        });

        // Clear filter button to remove any applied filters
        btnClearFilters.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFilterSelected(""); // Send empty ID to indicate filter removal
            }
            dismiss(); // Close the bottom sheet
        });

        return view;
    }
}

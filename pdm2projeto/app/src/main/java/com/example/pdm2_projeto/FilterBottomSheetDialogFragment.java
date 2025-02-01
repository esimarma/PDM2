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

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private List<LocationCategory> categories = new ArrayList<>();
    private String selectedCategoryId = "";
    private FilterListener filterListener;

    public interface FilterListener {
        void onFilterSelected(String categoryId);
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.locations_filter, container, false);

        Spinner categorySpinner = view.findViewById(R.id.spinner_categories);
        Button applyButton = view.findViewById(R.id.btn_apply_filters);

        LocationCategoryRepository repository = new LocationCategoryRepository();

        // Buscar categorias do Firestore
        repository.getCategories(new LocationCategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(List<LocationCategory> fetchedCategories) {
                categories = fetchedCategories;

                // Converter categorias para uma lista de nomes
                List<String> categoryNames = new ArrayList<>();
                for (LocationCategory category : categories) {
                    categoryNames.add(category.getDescription());
                }

                // Criar adaptador para o Spinner
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categoryNames);
                categorySpinner.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Erro ao carregar categorias", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para capturar a seleção
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategoryId = categories.get(position).getId(); // Pegando o ID correto
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = "";
            }
        });

        // Botão de aplicar filtro
        applyButton.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFilterSelected(selectedCategoryId);
            }
            dismiss();
        });

        Button btnClearFilters = view.findViewById(R.id.btn_remove_filters);
        btnClearFilters.setOnClickListener(v -> {
            if (filterListener != null) {
                filterListener.onFilterSelected(""); // Enviar ID vazio para remover filtros
            }
            dismiss(); // Fechar o BottomSheet
        });

        return view;
    }
}
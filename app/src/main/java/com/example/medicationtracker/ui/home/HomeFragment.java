package com.example.medicationtracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.databinding.FragmentHomeBinding;

import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HomeMedicineAdapter adapter;

    private TextView takenMedicineTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(HomeViewModel.class);
        takenMedicineTextView = binding.takenMedicineTodayTextView;
        adapter = new HomeMedicineAdapter();

        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        homeViewModel.getMedicines().observe(getViewLifecycleOwner(), medicinesList -> {
                if (medicinesList != null && !medicinesList.isEmpty()) {
                    binding.medicineRecyclerView.setVisibility(View.VISIBLE);
                    binding.takenMedicineTodayTextView.setVisibility(View.VISIBLE);

                    Map<Integer, Boolean> taken = homeViewModel.getTakenMap().getValue();
                    adapter.submitList(medicinesList, taken);

                } else {
                    binding.medicineRecyclerView.setVisibility(View.GONE);
                    binding.takenMedicineTodayTextView.setText("No medicines added yet. Add some to start tracking");
                }

        });

        homeViewModel.getTakenMap().observe(getViewLifecycleOwner(), takenMap -> {
            if (takenMap != null && !takenMap.isEmpty()) {
                boolean allTaken = true;

                for (Boolean taken : takenMap.values()) {
                    if (!taken) {
                        allTaken = false;
                        break;
                    }
                }

                if (allTaken) {
                    takenMedicineTextView.setText("Congratulations! You have taken all your medicines today!");
                }
            }
        });




        homeViewModel.getTakenMap().observe(getViewLifecycleOwner(), map ->
                adapter.submitList(homeViewModel.getMedicines().getValue(), map)
        );

        adapter.setOnTakeClickListener(medicine -> homeViewModel.markTaken(medicine.getId()));
    }
}

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

import java.util.List;
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

        homeViewModel.getMedicines().observe(getViewLifecycleOwner(), medicines -> updateUI());
        homeViewModel.getTakenMap().observe(getViewLifecycleOwner(), taken -> updateUI());


        adapter.setOnTakeClickListener(medicine -> homeViewModel.markTaken(medicine.getId()));
    }

    private void updateUI() {
        Map<Integer, Boolean> taken = homeViewModel.getTakenMap().getValue();
        List<Medicine> medicines = homeViewModel.getMedicines().getValue();

        if (medicines == null || medicines.isEmpty()) {
            binding.medicineRecyclerView.setVisibility(View.GONE);
            binding.takenMedicineTodayTextView.setText("No medicines added yet. Add some to start tracking");
            return;
        }

        binding.medicineRecyclerView.setVisibility(View.VISIBLE);

        adapter.submitList(medicines, taken);

        if (taken != null && !taken.isEmpty()) {
            boolean allTaken = true;

            for (Medicine medicine: medicines) {
                Boolean takenStatus = taken != null? taken.get(medicine.getId()) : null;
                if (takenStatus == null || !takenStatus) {
                    allTaken = false;
                    break;
                }
            }

            if (allTaken) {
                takenMedicineTextView.setText("Congratulations! You have taken all your medicines today!");
            } else {
                takenMedicineTextView.setText("You have medicines left to take today.");
            }
        } else {
            takenMedicineTextView.setText("Have you Taken Your medicines yet?");
        }
    }
}

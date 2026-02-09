package com.example.medicationtracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medicationtracker.data.DailyDoseStatus;
import com.example.medicationtracker.databinding.FragmentHomeBinding;

import java.util.List;

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

        homeViewModel.getTodayDoses().observe(getViewLifecycleOwner(), this::updateUI);

        adapter.setListener(new HomeMedicineAdapter.OnDoseActionListener() {
            @Override
            public void onTake(DailyDoseStatus item) {
                homeViewModel.markTaken(item);
            }

            @Override
            public void onUndo(DailyDoseStatus item) {
                homeViewModel.undoTaken(item);
            }
        });


    }

    private void updateUI(List<DailyDoseStatus> doses) {

        if (doses == null || doses.isEmpty()) {
            binding.medicineRecyclerView.setVisibility(View.GONE);
            binding.takenMedicineTodayTextView
                    .setText("No doses scheduled for today.");
            return;
        }

        binding.medicineRecyclerView.setVisibility(View.VISIBLE);
        adapter.submitList(doses);

        boolean allTaken = true;

        for (DailyDoseStatus dose : doses) {
            if (!dose.isTaken()) {
                allTaken = false;
                break;
            }
        }

        if (allTaken) {
            takenMedicineTextView.setText(
                    "🎉 Congratulations! You’ve taken all your doses today!"
            );
        } else {
            takenMedicineTextView.setText(
                    "You still have doses to take today."
            );
        }
    }

}

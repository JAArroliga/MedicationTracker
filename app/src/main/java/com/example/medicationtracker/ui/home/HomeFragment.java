package com.example.medicationtracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicationtracker.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    HomeViewModel homeViewModel;



    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.getMedicineTaken().observe(
                getViewLifecycleOwner(),
                medicineTaken -> {
                    if (medicineTaken) {
                        binding.takenMedicineTextView.setText("Medicine Taken");
                        binding.takenMedicineButton.setEnabled(false);
                    } else {
                        binding.takenMedicineTextView.setText("Medicine Not Taken");
                        binding.takenMedicineButton.setEnabled(true);
                    }
                }
        );
        binding.takenMedicineButton.setOnClickListener(v -> homeViewModel.markTaken());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
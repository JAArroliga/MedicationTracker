package com.example.medicationtracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private HomeMedicineAdapter adapter;

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

        adapter = new HomeMedicineAdapter();
        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        homeViewModel.getMedicines().observe(getViewLifecycleOwner(), medicines ->
                adapter.submitList(medicines, homeViewModel.getTakenMap().getValue())
        );

        homeViewModel.getTakenMap().observe(getViewLifecycleOwner(), map ->
                adapter.submitList(homeViewModel.getMedicines().getValue(), map)
        );

        adapter.setOnTakeClickListener(medicine -> homeViewModel.markTaken(medicine.getId()));
    }
}

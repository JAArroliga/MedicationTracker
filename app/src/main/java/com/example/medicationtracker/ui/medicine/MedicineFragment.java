package com.example.medicationtracker.ui.medicine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.medicationtracker.R;
import com.example.medicationtracker.databinding.FragmentMedicineBinding;

public class MedicineFragment extends Fragment {

    private FragmentMedicineBinding binding;
    private MedicineViewModel viewModel;

    public MedicineFragment() {
        super(R.layout.fragment_medicine);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MedicineViewModel galleryViewModel =
                new ViewModelProvider(this).get(MedicineViewModel.class);

        binding = FragmentMedicineBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentMedicineBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);


        viewModel.getMedicines().observe(getViewLifecycleOwner(), this::renderMedicines);

        binding.addMedicineButton.setOnClickListener(v -> {
            String name = binding.medicineInput.getText().toString();

        });



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
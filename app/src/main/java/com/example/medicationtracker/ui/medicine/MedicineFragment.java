package com.example.medicationtracker.ui.medicine;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;
import com.example.medicationtracker.databinding.FragmentMedicineBinding;

import java.util.List;

public class MedicineFragment extends Fragment {

    private FragmentMedicineBinding binding;
    private MedicineViewModel viewModel;
    private MedicineAdapter adapter;


    public MedicineFragment() {
        super(R.layout.fragment_medicine);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentMedicineBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);
        adapter = new MedicineAdapter();

        viewModel.getAllMedicines().observe(getViewLifecycleOwner(), medicines -> adapter.submitList(medicines));

        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        binding.addMedicineButton.setOnClickListener(v -> {
            String name = binding.medicineInput.getText().toString();
            String dosage = binding.dosageInput.getText().toString();
            String time = binding.timeInput.getText().toString();

            if (!name.isEmpty() && !dosage.isEmpty() && !time.isEmpty()) {
                viewModel.addMedicine(name, dosage, time);
                binding.medicineInput.setText("");
            }
        });

    }

    private void renderMedicines(List<Medicine> medicines) {
        StringBuilder builder = new StringBuilder();
        for (Medicine medicine : medicines) {
            builder.append("• ")
                    .append(medicine.toString())
                    .append("\n");
        }
    }

    private void clearInputs() {
        binding.medicineInput.setText("");
        binding.dosageInput.setText("");
        binding.timeInput.setText("");

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
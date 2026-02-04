package com.example.medicationtracker.ui.medicine;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;
import com.example.medicationtracker.databinding.FragmentMedicineBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicineFragment extends Fragment {

    private FragmentMedicineBinding binding;
    private MedicineViewModel viewModel;
    private MedicineAdapter adapter;
    private Medicine editingMedicine = null;
    private String selectedTime = "";

    public MedicineFragment() {
        super(R.layout.fragment_medicine);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentMedicineBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);
        adapter = new MedicineAdapter();

        viewModel.getAllMedicines().observe(getViewLifecycleOwner(), medicines -> adapter.submitList(medicines));

        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        ArrayAdapter<CharSequence> unitAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.dosage_units,
                        android.R.layout.simple_spinner_item
                );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.dosageUnitSpinner.setAdapter(unitAdapter);

        binding.timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (timePickerView, hourOfDay, minute1) -> {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute1);
                SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                selectedTime = format.format(c.getTime());
                binding.timeLabel.setText(selectedTime);
            }, hour, minute, false);
            timePickerDialog.show();
        });

        binding.addMedicineButton.setOnClickListener(v -> {
            String name = binding.medicineInput.getText().toString().trim();
            String amountText = binding.dosageAmountInput.getText().toString().trim();
            String unit = binding.dosageUnitSpinner.getSelectedItem().toString();
            String time = binding.timeLabel.getText().toString().trim();

            if (name.isEmpty()) {
                binding.medicineInput.setError("Required");
                return;
            }

            if (amountText.isEmpty()) {
                binding.dosageAmountInput.setError("Required");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                binding.dosageAmountInput.setError("Invalid number");
                return;
            }

            if (time.isEmpty() || time.equals("Time")) {
                Toast.makeText(requireContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editingMedicine != null) {
                Medicine updatedMedicine = new Medicine(editingMedicine.getId(), name, amount, unit, time);
                viewModel.updateMedicine(updatedMedicine);
                editingMedicine = null;
            } else {
                viewModel.addMedicine(0, name, amount, unit, time);
            }

            clearInputs();
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Medicine medicine = adapter.getMedicineAt(viewHolder.getAdapterPosition());
                viewModel.deleteMedicine(medicine);
                Toast.makeText(requireContext(), "Medicine deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.medicineRecyclerView);

        adapter.setOnItemClickListener(medicine -> {
            binding.medicineInput.setText(medicine.getName());

            String formattedDosage = medicine.getFormattedDosage();
            if (formattedDosage != null) {
                String[] parts = formattedDosage.split(" ");
                if (parts.length >= 2) {
                    binding.dosageAmountInput.setText(parts[0]);

                    ArrayAdapter spinnerAdapter = (ArrayAdapter) binding.dosageUnitSpinner.getAdapter();
                    int position = spinnerAdapter.getPosition(parts[1]);
                    if (position >= 0) {
                        binding.dosageUnitSpinner.setSelection(position);
                    }
                }
            }

            binding.timeLabel.setText(medicine.getTime());
            editingMedicine = medicine;
        });
    }

    private void clearInputs() {
        binding.medicineInput.setText("");
        binding.dosageAmountInput.setText("");
        binding.dosageUnitSpinner.setSelection(0);
        binding.timeLabel.setText("Time");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

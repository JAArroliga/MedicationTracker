package com.example.medicationtracker.ui.medicine;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class MedicineFragment extends Fragment {

    private FragmentMedicineBinding binding;
    private MedicineViewModel viewModel;
    private MedicineAdapter adapter;
    private Medicine editingMedicine = null;
    private final List<String> selectedTimes = new ArrayList<>();

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

        // Medicine type & frequency spinners with placeholders
        setupSpinnerWithPlaceholder(binding.dosageUnitSpinner, R.array.dosage_units);
        setupSpinnerWithPlaceholder(binding.medicineTypeSpinner, R.array.medicine_types);
        setupSpinnerWithPlaceholder(binding.frequencySpinner, R.array.medicine_frequencies);

        binding.timeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog =
                    new TimePickerDialog(requireContext(), (view1, hourOfDay, minute1) -> {

                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute1);

                        SimpleDateFormat format =
                                new SimpleDateFormat("hh:mm a", Locale.getDefault());

                        String time = format.format(c.getTime());

                        if (!selectedTimes.contains(time)) {
                            selectedTimes.add(time);
                        }

                    }, hour, minute, false);

            timePickerDialog.show();
        });

        binding.addMedicineButton.setOnClickListener(v -> {
            String name = binding.medicineInput.getText().toString().trim();
            String amountText = binding.dosageAmountInput.getText().toString().trim();
            String unit = binding.dosageUnitSpinner.getSelectedItem().toString();
            String type = binding.medicineTypeSpinner.getSelectedItem().toString();
            String frequency = binding.frequencySpinner.getSelectedItem().toString();

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

            if (selectedTimes.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Please add at least one time",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (editingMedicine != null) {
                Medicine updatedMedicine = new Medicine(
                        editingMedicine.getId(),
                        name,
                        amount,
                        unit,
                        type,
                        frequency,
                        new ArrayList<>(selectedTimes)
                );
                viewModel.updateMedicine(updatedMedicine);
                editingMedicine = null;
            } else {
                viewModel.addMedicine(
                        0,
                        name,
                        amount,
                        unit,
                        type,
                        frequency,
                        new ArrayList<>(selectedTimes)
                );
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

            selectedTimes.clear();
            selectedTimes.addAll(medicine.getTimes());

            ArrayAdapter typeSpinnerAdapter =
                    (ArrayAdapter) binding.medicineTypeSpinner.getAdapter();
            int typePosition = typeSpinnerAdapter.getPosition(medicine.getType());
            if (typePosition >= 0) {
                binding.medicineTypeSpinner.setSelection(typePosition);
            }

            ArrayAdapter frequencySpinnerAdapter =
                    (ArrayAdapter) binding.frequencySpinner.getAdapter();
            int frequencyPosition = frequencySpinnerAdapter.getPosition(medicine.getFrequency());
            if (frequencyPosition >= 0) {
                binding.frequencySpinner.setSelection(frequencyPosition);
            }

            editingMedicine = medicine;
        });
    }

    private void setupSpinnerWithPlaceholder(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getTextArray(arrayRes)
        ) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item (placeholder)
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    // Gray out the first item
                    ((TextView) view).setTextColor(0xFF888888);
                } else {
                    ((TextView) view).setTextColor(0xFF000000);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0); // default to placeholder
    }


    private void clearInputs() {
        binding.medicineInput.setText("");
        binding.dosageAmountInput.setText("");
        binding.dosageUnitSpinner.setSelection(0);
        binding.medicineTypeSpinner.setSelection(0);
        binding.frequencySpinner.setSelection(0);
        selectedTimes.clear();
        binding.timesContainer.removeAllViews();
        editingMedicine = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void renderTimes() {
        binding.timesContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (String time : selectedTimes) {
            View chip = inflater.inflate(R.layout.item_time_chip, binding.timesContainer, false);

            TextView timeText = chip.findViewById(R.id.timeText);
            View remove = chip.findViewById(R.id.removeTime);

            timeText.setText(time);

            remove.setOnClickListener(v -> {
                selectedTimes.remove(time);
                renderTimes();
            });

            binding.timesContainer.addView(chip);
        }
    }

}

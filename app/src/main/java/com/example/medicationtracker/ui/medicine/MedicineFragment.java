package com.example.medicationtracker.ui.medicine;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;
import com.example.medicationtracker.databinding.FragmentMedicineBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

        // Observe Medicines with Doses
        viewModel.getMedicinesWithDoses().observe(getViewLifecycleOwner(), adapter::submitList);

        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        setupSpinners();

        binding.timeButton.setOnClickListener(v -> showTimePicker());

        binding.addMedicineButton.setOnClickListener(v -> addOrUpdateMedicine());

        setupSwipeToDelete();

        adapter.setOnItemClickListener(this::editMedicine);
    }

    private void setupSpinners() {
        setupSpinnerWithPlaceholder(binding.dosageUnitSpinner, R.array.dosage_units);
        setupSpinnerWithPlaceholder(binding.medicineTypeSpinner, R.array.medicine_types);
        setupSpinnerWithPlaceholder(binding.frequencySpinner, R.array.medicine_frequencies);

        binding.frequencySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String frequency = parent.getItemAtPosition(position).toString();
                        onFrequencyChanged(frequency);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                }
        );
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute1);

            String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(c.getTime());

            String frequency = binding.frequencySpinner.getSelectedItem().toString();

            if (frequency.equalsIgnoreCase("Once daily")) {
                selectedTimes.clear();
            }
            if (!selectedTimes.contains(time)) selectedTimes.add(time);

            Collections.sort(selectedTimes);
            renderTimes();

        }, hour, minute, false).show();
    }

    private void addOrUpdateMedicine() {
        String name = binding.medicineInput.getText().toString().trim();
        String amountText = binding.dosageAmountInput.getText().toString().trim();
        String unit = binding.dosageUnitSpinner.getSelectedItem().toString();
        String type = binding.medicineTypeSpinner.getSelectedItem().toString();
        String frequency = binding.frequencySpinner.getSelectedItem().toString();

        if (name.isEmpty()) { binding.medicineInput.setError("Required"); return; }
        if (amountText.isEmpty()) { binding.dosageAmountInput.setError("Required"); return; }
        if (selectedTimes.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one time", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try { amount = Double.parseDouble(amountText); }
        catch (NumberFormatException e) { binding.dosageAmountInput.setError("Invalid number"); return; }

        if (frequency.equalsIgnoreCase("Once daily") && selectedTimes.size() != 1) {
            Toast.makeText(requireContext(),
                    "Once-daily medicines must have exactly one time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingMedicine != null) {
            viewModel.updateMedicine(editingMedicine, new ArrayList<>(selectedTimes));
            editingMedicine = null;
        } else {
            viewModel.addMedicine(name, amount, unit, type, frequency, new ArrayList<>(selectedTimes));
        }

        clearInputs();
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                MedicineWithDoses item = adapter.getItemAt(viewHolder.getAdapterPosition());
                viewModel.deleteMedicine(item.medicine);
                Toast.makeText(requireContext(), "Medicine deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.medicineRecyclerView);
    }

    private void editMedicine(MedicineWithDoses item) {
        Medicine medicine = item.medicine;

        binding.medicineInput.setText(medicine.getName());
        binding.dosageAmountInput.setText(String.valueOf(medicine.getDosageAmount()));

        ArrayAdapter unitAdapter = (ArrayAdapter) binding.dosageUnitSpinner.getAdapter();
        int unitPos = unitAdapter.getPosition(medicine.getDosageUnit());
        if (unitPos >= 0) binding.dosageUnitSpinner.setSelection(unitPos);

        ArrayAdapter typeAdapter = (ArrayAdapter) binding.medicineTypeSpinner.getAdapter();
        int typePos = typeAdapter.getPosition(medicine.getType());
        if (typePos >= 0) binding.medicineTypeSpinner.setSelection(typePos);

        ArrayAdapter freqAdapter = (ArrayAdapter) binding.frequencySpinner.getAdapter();
        int freqPos = freqAdapter.getPosition(medicine.getFrequency());
        if (freqPos >= 0) binding.frequencySpinner.setSelection(freqPos);

        selectedTimes.clear();
        for (Dose dose : item.doses) {
            selectedTimes.add(dose.getTime());
        }
        Collections.sort(selectedTimes);
        renderTimes();

        editingMedicine = medicine;
    }

    private void setupSpinnerWithPlaceholder(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getTextArray(arrayRes)) {
            @Override
            public boolean isEnabled(int position) { return position != 0; }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView)view).setTextColor(position == 0 ? 0xFF888888 : 0xFF000000);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
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

    private void onFrequencyChanged(String frequency) {
        if (frequency.equalsIgnoreCase("Once daily") && selectedTimes.size() > 1) {
            Collections.sort(selectedTimes);
            String keptTime = selectedTimes.get(0);
            selectedTimes.clear();
            selectedTimes.add(keptTime);
            renderTimes();
            Toast.makeText(requireContext(),
                    "Extra times removed for once-daily medicine", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


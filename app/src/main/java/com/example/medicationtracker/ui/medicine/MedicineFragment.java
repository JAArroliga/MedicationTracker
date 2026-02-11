package com.example.medicationtracker.ui.medicine;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
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

    // Binding and ViewModel
    private FragmentMedicineBinding binding;
    private MedicineViewModel viewModel;

    // Adapter and currently editing medicine
    private MedicineAdapter adapter;
    private Medicine editingMedicine = null;

    // Times selected for doses
    private final List<String> selectedTimes = new ArrayList<>();

    // Checkboxes for days of week
    private CheckBox mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox,
            thursdayCheckBox, fridayCheckBox, saturdayCheckBox, sundayCheckBox;
    private HorizontalScrollView daysOfWeekScroll;

    public MedicineFragment() {
        super(R.layout.fragment_medicine);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = FragmentMedicineBinding.bind(view);
        viewModel = new ViewModelProvider(this).get(MedicineViewModel.class);

        // Setup adapter and RecyclerView
        adapter = new MedicineAdapter();
        viewModel.getMedicinesWithDoses().observe(getViewLifecycleOwner(), adapter::submitList);
        binding.medicineRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.medicineRecyclerView.setAdapter(adapter);

        // Setup UI components
        setupSpinners();
        setupDayCheckboxes(view);
        binding.timeButton.setOnClickListener(v -> showTimePicker());
        binding.addMedicineButton.setOnClickListener(v -> addOrUpdateMedicine());
        setupSwipeToDelete();

        // Adapter click
        adapter.setOnItemClickListener(this::editMedicine);
    }

    // ======================== UI SETUP ========================
    private void setupSpinners() {
        setupSpinnerWithPlaceholder(binding.dosageUnitSpinner, R.array.dosage_units);
        setupSpinnerWithPlaceholder(binding.medicineTypeSpinner, R.array.medicine_types);
        setupSpinnerWithPlaceholder(binding.frequencySpinner, R.array.medicine_frequencies);

        binding.frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                updateDaysOfWeekVisibility(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateDaysOfWeekVisibility("");
            }
        });
    }

    private void setupDayCheckboxes(View view) {
        daysOfWeekScroll = view.findViewById(R.id.daysOfWeekScroll);

        mondayCheckBox = view.findViewById(R.id.checkMon);
        tuesdayCheckBox = view.findViewById(R.id.checkTue);
        wednesdayCheckBox = view.findViewById(R.id.checkWed);
        thursdayCheckBox = view.findViewById(R.id.checkThu);
        fridayCheckBox = view.findViewById(R.id.checkFri);
        saturdayCheckBox = view.findViewById(R.id.checkSat);
        sundayCheckBox = view.findViewById(R.id.checkSun);
    }

    private void setupSpinnerWithPlaceholder(Spinner spinner, int arrayRes) {
        ArrayAdapter<CharSequence> adapter =
                new ArrayAdapter<>(requireContext(), R.layout.spinner_item, getResources().getTextArray(arrayRes));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
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

    // ======================== MEDICINE CRUD ========================
    private void addOrUpdateMedicine() {
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
        if (selectedTimes.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one time", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            binding.dosageAmountInput.setError("Invalid number");
            return;
        }

        int daysMask;

        if ("Specific days of week".equalsIgnoreCase(frequency)) {
            daysMask = buildDaysOfWeekMask();
            if (daysMask == 0) {
                Toast.makeText(requireContext(), "Please select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Daily medications automatically include all days
            daysMask = 0b1111111;
            resetDayCheckboxes();
        }

        if (editingMedicine != null) {
            viewModel.updateMedicine(editingMedicine, frequency, new ArrayList<>(selectedTimes), daysMask);
            editingMedicine = null;
        } else {
            viewModel.addMedicine(name, amount, unit, type, frequency, new ArrayList<>(selectedTimes), daysMask);
        }

        clearInputs();
    }

    private void editMedicine(MedicineWithDoses item) {
        Medicine medicine = item.medicine;

        binding.medicineInput.setText(medicine.getName());
        binding.dosageAmountInput.setText(String.valueOf(medicine.getDosageAmount()));

        // Restore spinner selections
        setSpinnerSelection(binding.dosageUnitSpinner, medicine.getDosageUnit());
        setSpinnerSelection(binding.medicineTypeSpinner, medicine.getType());
        setSpinnerSelection(binding.frequencySpinner, medicine.getFrequency());

        selectedTimes.clear();
        for (Dose dose : item.doses) {
            selectedTimes.add(dose.getTime());
        }
        Collections.sort(selectedTimes);
        renderTimes();

        editingMedicine = medicine;

        if ("Specific days of week".equalsIgnoreCase(medicine.getFrequency())) {
            int mask = medicine.getDaysOfWeekMask();
            sundayCheckBox.setChecked((mask & (1 << 0)) != 0);
            mondayCheckBox.setChecked((mask & (1 << 1)) != 0);
            tuesdayCheckBox.setChecked((mask & (1 << 2)) != 0);
            wednesdayCheckBox.setChecked((mask & (1 << 3)) != 0);
            thursdayCheckBox.setChecked((mask & (1 << 4)) != 0);
            fridayCheckBox.setChecked((mask & (1 << 5)) != 0);
            saturdayCheckBox.setChecked((mask & (1 << 6)) != 0);
        } else {
            resetDayCheckboxes();
        }

        updateDaysOfWeekVisibility(medicine.getFrequency());
    }

    // ======================== TIME PICKER ========================
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
            if (frequency.equalsIgnoreCase("Once daily")) selectedTimes.clear();

            if (!selectedTimes.contains(time)) selectedTimes.add(time);
            Collections.sort(selectedTimes);
            renderTimes();

        }, hour, minute, false).show();
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

    // ======================== CHECKBOX HELPERS ========================
    private int buildDaysOfWeekMask() {
        int mask = 0;
        if (mondayCheckBox.isChecked()) mask |= 1 << 1;
        if (tuesdayCheckBox.isChecked()) mask |= 1 << 2;
        if (wednesdayCheckBox.isChecked()) mask |= 1 << 3;
        if (thursdayCheckBox.isChecked()) mask |= 1 << 4;
        if (fridayCheckBox.isChecked()) mask |= 1 << 5;
        if (saturdayCheckBox.isChecked()) mask |= 1 << 6;
        if (sundayCheckBox.isChecked()) mask |= 1 << 0;
        return mask;
    }

    private void resetDayCheckboxes() {
        mondayCheckBox.setChecked(false);
        tuesdayCheckBox.setChecked(false);
        wednesdayCheckBox.setChecked(false);
        thursdayCheckBox.setChecked(false);
        fridayCheckBox.setChecked(false);
        saturdayCheckBox.setChecked(false);
        sundayCheckBox.setChecked(false);
    }

    private void updateDaysOfWeekVisibility(String frequency) {
        if ("Specific days of week".equalsIgnoreCase(frequency)) {
            daysOfWeekScroll.setVisibility(View.VISIBLE);
        } else {
            daysOfWeekScroll.setVisibility(View.GONE);
            resetDayCheckboxes();
        }
    }

    // ======================== UTIL ========================
    private void clearInputs() {
        binding.medicineInput.setText("");
        binding.dosageAmountInput.setText("");
        binding.dosageUnitSpinner.setSelection(0);
        binding.medicineTypeSpinner.setSelection(0);
        binding.frequencySpinner.setSelection(0);
        selectedTimes.clear();
        binding.timesContainer.removeAllViews();
        resetDayCheckboxes();
        editingMedicine = null;
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        int pos = adapter.getPosition(value);
        if (pos >= 0) spinner.setSelection(pos);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}



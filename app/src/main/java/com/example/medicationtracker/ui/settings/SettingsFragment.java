package com.example.medicationtracker.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medicationtracker.R;
import com.example.medicationtracker.data.MedicineRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    private SettingsManager settingsManager;
    private MedicineRepository repository;
    private SwitchMaterial switchReminders;
    private EditText editSnoozeMinutes;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsManager = new SettingsManager(requireContext());
        repository = new MedicineRepository(requireActivity().getApplication());

        switchReminders = view.findViewById(R.id.switchReminders);
        editSnoozeMinutes = view.findViewById(R.id.editSnoozeMinutes);

        switchReminders.setChecked(settingsManager.areRemindersEnabled());
        editSnoozeMinutes.setText(String.valueOf(settingsManager.getSnoozeMinutes()));

        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setRemindersEnabled(isChecked);
            if (isChecked) {
                repository.scheduleAllAlarms();
            } else {
                repository.cancelAllAlarms();
            }
        });

        editSnoozeMinutes.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveSnoozeValue();
            }
        });
    }

    private void saveSnoozeValue() {
        String snoozeValue = editSnoozeMinutes.getText() != null ? editSnoozeMinutes.getText().toString().trim() : "";

        if (TextUtils.isEmpty(snoozeValue)) return;

        int minutes;

        try {
            minutes = Integer.parseInt(snoozeValue);
        } catch (NumberFormatException e) {
            minutes = 10;
        }

        if (minutes < 1) minutes = 1;
        if (minutes > 60) minutes = 60;

        settingsManager.setSnoozeMinutes(minutes);
        editSnoozeMinutes.setText(String.valueOf(minutes));
    }
}

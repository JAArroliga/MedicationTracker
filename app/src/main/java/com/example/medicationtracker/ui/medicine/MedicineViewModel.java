package com.example.medicationtracker.ui.medicine;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.MedicineRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicineViewModel extends AndroidViewModel {

    private final MedicineRepository repository;
    private final LiveData<List<MedicineWithDoses>> medicinesWithDoses;

    public MedicineViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
        medicinesWithDoses = repository.getMedicinesWithDoses();
    }

    public LiveData<List<MedicineWithDoses>> getMedicinesWithDoses() {
        return medicinesWithDoses;
    }

    public void addMedicine(String name, double dosageAmount, String dosageUnit, String type, String frequency, List<String> times, int daysOfWeekMask) {
        List<String> normalizedTimes = normalizeTimes(frequency, times);
        Medicine medicine = new Medicine(0, name, dosageAmount, dosageUnit, type, frequency, daysOfWeekMask);
        repository.insertMedicineWithDoses(medicine, times);
    }

    public void updateMedicine(Medicine medicine,String frequency, List<String> times, int daysOfWeekMask) {
        medicine.setFrequency(frequency);
        medicine.setDaysOfWeekMask(daysOfWeekMask);
        repository.updateMedicineWithDoses(medicine, normalizeTimes(frequency, times));
    }

    public void deleteMedicine(Medicine medicine) {
        repository.delete(medicine);
    }

    private List<String> normalizeTimes(String frequency, List<String> times) {
        if (frequency.equalsIgnoreCase("Once daily") && times.size() > 1) {
            Collections.sort(times);
            return new ArrayList<>(Collections.singletonList(times.get(0)));
        }
        return times;
    }
}


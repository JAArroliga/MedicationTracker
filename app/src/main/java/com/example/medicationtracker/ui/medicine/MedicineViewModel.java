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

    public void addMedicine(String name, double dosageAmount, String dosageUnit, String type, String frequency, List<String> times) {
        Medicine medicine = new Medicine(0, name, dosageAmount, dosageUnit, type, frequency);
        repository.insertMedicineWithDoses(medicine, times);
    }

    public void updateMedicine(Medicine medicine, List<String> times) {
        repository.updateMedicineWithDoses(medicine, times);
    }

    public void deleteMedicine(Medicine medicine) {
        repository.delete(medicine);
    }
}


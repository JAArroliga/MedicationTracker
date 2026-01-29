package com.example.medicationtracker.ui.medicine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medicationtracker.Medicine;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class MedicineViewModel extends ViewModel {

    private final MutableLiveData<List<Medicine>> medicines = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Medicine>> getMedicines() {
        return medicines;
    }

    public void addMedicine(Medicine medicine) {
     List<Medicine> current = medicines.getValue();
     if (current == null) return;

     current = new ArrayList<>(current);
     current.add(medicine);
     medicines.setValue(current);
    }
}
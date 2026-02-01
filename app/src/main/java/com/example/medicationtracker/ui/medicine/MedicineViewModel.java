package com.example.medicationtracker.ui.medicine;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.MedicineRepository;

import java.util.List;

public class MedicineViewModel extends AndroidViewModel {

    private MedicineRepository repository;
    private LiveData<List<Medicine>> allMedicines;

    public MedicineViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
        allMedicines = repository.getAllMedicines();
    }

    public void insert(Medicine medicine) {
        repository.insert(medicine);
    }

    public LiveData<List<Medicine>> getAllMedicines() {
        return allMedicines;
    }

    public void addMedicine(int id, String name, String dosage, String time) {
        Medicine medicine = new Medicine(id, name, dosage, time);
        repository.insert(medicine);
    }

    public void deleteMedicine(Medicine medicine) {
        repository.delete(medicine);
    }

    public void updateMedicine(Medicine medicine) {
        repository.update(medicine);
    }
}

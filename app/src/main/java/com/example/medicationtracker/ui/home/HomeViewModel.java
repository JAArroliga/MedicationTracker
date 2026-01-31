package com.example.medicationtracker.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.MedicineRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    private final MedicineRepository repository;
    private final LiveData<List<Medicine>> medicines;
    private final LiveData<Map<Integer, Boolean>> takenMap;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
        medicines = repository.getAllMedicines();
        takenMap = repository.getTakenMapForToday();
    }

    public LiveData<List<Medicine>> getMedicines() {
        return medicines;
    }

    public LiveData<Map<Integer, Boolean>> getTakenMap() {
        return takenMap;
    }

    public void markTaken(int medicineId) {
        repository.markTaken(medicineId, true);

        Map<Integer, Boolean> current = takenMap.getValue();
        Map<Integer, Boolean> updated = current == null ? new HashMap<>() : new HashMap<>(current);
        updated.put(medicineId, true);
        ((MutableLiveData<Map<Integer, Boolean>>) takenMap).postValue(updated);
    }
}

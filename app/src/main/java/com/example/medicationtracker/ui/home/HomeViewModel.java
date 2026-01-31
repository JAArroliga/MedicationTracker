package com.example.medicationtracker.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.MedicineDao;
import com.example.medicationtracker.data.MedicineDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    private MedicineDao medicineDao;
    private LiveData<List<Medicine>> medicines;

    private final MutableLiveData<Map<Integer, Boolean>> takenMap = new MutableLiveData<>(new HashMap<>());

    public HomeViewModel(Application application) {
        super(application);
        medicineDao = MedicineDatabase.getInstance(application).medicineDao();
        medicines = medicineDao.getAllMedicines();
    }

    public LiveData<List<Medicine>> getMedicines() {
        return medicines;
    }

    public LiveData<Map<Integer, Boolean>> getTakenMap() {
        return takenMap;
    }

    public void markTaken(int medicineId) {
        Map<Integer, Boolean> map = takenMap.getValue();
        if (map == null) map = new HashMap<>();
        map.put(medicineId, true);
        takenMap.setValue(map);
    }

}

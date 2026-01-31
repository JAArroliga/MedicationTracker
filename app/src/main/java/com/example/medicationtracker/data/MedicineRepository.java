package com.example.medicationtracker.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.TakenTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineRepository {

    private MedicineDao medicineDao;
    private TakenTableDao takenTableDao;
    private LiveData<List<Medicine>> allMedicines;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public MedicineRepository(Application application) {
        MedicineDatabase database = MedicineDatabase.getInstance(application);
        medicineDao = database.medicineDao();
        allMedicines = medicineDao.getAllMedicines();
        takenTableDao = database.takenTableDao();
    }


    public void insert(Medicine medicine) {
        executor.execute(() -> medicineDao.insert(medicine));
    }

    public void delete(Medicine medicine) {
        executor.execute(() -> medicineDao.delete(medicine));
    }

    public LiveData<List<Medicine>> getAllMedicines() {
        return allMedicines;
    }

    public void update(Medicine medicine) {
        executor.execute(() -> medicineDao.update(medicine));
    }

    public LiveData<Map<Integer, Boolean>> getTakenMapForToday() {
        MutableLiveData<Map<Integer, Boolean>> liveMap = new MutableLiveData<>();
        String today = java.time.LocalDate.now().toString();

        executor.execute(() -> {
            List<TakenTable> list = takenTableDao.getTakenMapForDate(today);
            Map<Integer, Boolean> map = new HashMap<>();
            for (TakenTable mt : list) {
                map.put(mt.getMedicineId(), mt.isTaken());
            }
            liveMap.postValue(map);
        });

        return liveMap;
    }

    public void markTaken(int medicineId, boolean taken) {
        String today = java.time.LocalDate.now().toString();
        Executors.newSingleThreadExecutor().execute(() -> {
            takenTableDao.insert(new TakenTable(medicineId, today, taken));
        });
    }
}

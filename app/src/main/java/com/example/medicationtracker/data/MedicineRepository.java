package com.example.medicationtracker.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.medicationtracker.Medicine;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineRepository {

    private MedicineDao medicineDao;
    private LiveData<List<Medicine>> allMedicines;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public MedicineRepository(Application application) {
        MedicineDatabase database = MedicineDatabase.getInstance(application);
        medicineDao = database.medicineDao();
        allMedicines = medicineDao.getAllMedicines();

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
}

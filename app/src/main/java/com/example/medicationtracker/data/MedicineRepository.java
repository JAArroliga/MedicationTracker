package com.example.medicationtracker.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.TakenTable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
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

    public LiveData<Map<LocalDate, DayStatus>> getMedicationStatusMap(YearMonth month) {
        MutableLiveData<Map<LocalDate, DayStatus>> statusMap = new MutableLiveData<>();

        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        executor.execute(() -> {
            Map<LocalDate, DayStatus> map = new HashMap<>();
            List<TakenTable> takenList = takenTableDao.getTakenMapForDateRange(start.toString(), end.toString());

            for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
                List<TakenTable> dayEntries = new ArrayList<>();
                for (TakenTable tt : takenList) {
                    if (LocalDate.parse(tt.getDate()).equals(current)) {
                        dayEntries.add(tt);
                    }
                }

                int totalMeds = medicineDao.getAllMedicines().getValue() != null ? medicineDao.getAllMedicines().getValue().size() : 0;
                int takenCount = 0;
                for (TakenTable tt : dayEntries) {
                    if (tt.isTaken()) {
                        takenCount++;
                    }
                }

                DayStatus status;
                if (takenCount == 0) {
                    status = DayStatus.NONE;
                } else if (takenCount < totalMeds) {
                    status = DayStatus.PARTIAL;
                } else {
                    status = DayStatus.ALL_TAKEN;
                }

                map.put(current, status);
            }

            statusMap.postValue(map);
        });

        return statusMap;
    }

    public List<Medicine> getMissedMedicine(LocalDate date) {
        List<Medicine> missedMedicines = new ArrayList<>();

        List<Medicine> expectedMedicines = medicineDao.getAllMedicinesList();
        List<TakenTable> takenEntries = takenTableDao.getTakenMapForDate(date.toString());
        Map<Integer, Boolean> takenMap = new HashMap<>();

        for (TakenTable tt : takenEntries) {
            takenMap.put(tt.getMedicineId(), tt.isTaken());
        }

        for (Medicine med : expectedMedicines) {
            Boolean wasTaken = takenMap.get(med.getId());

            if (wasTaken == null || !wasTaken) {
                missedMedicines.add(med);
            }
        }

        return missedMedicines;
    }

}

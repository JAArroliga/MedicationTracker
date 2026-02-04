package com.example.medicationtracker.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.TakenTable;
import com.example.medicationtracker.ui.calendar.DailyMedicationStatus;

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
        String today = LocalDate.now().toString();

        LiveData<List<TakenTable>> source =
                takenTableDao.getTakenForDateLive(today);

        MediatorLiveData<Map<Integer, Boolean>> result = new MediatorLiveData<>();

        result.addSource(source, list -> {
            Map<Integer, Boolean> map = new HashMap<>();
            if (list != null) {
                for (TakenTable t : list) {
                    map.put(t.getMedicineId(), t.isTaken());
                }
            }
            result.setValue(map);
        });

        return result;
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
            List<Medicine> allMeds = medicineDao.getAllMedicinesList();
            int totalMeds = allMeds.size();
            List<TakenTable> takenList = takenTableDao.getTakenMapForDateRange(start.toString(), end.toString());

            for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {

                List<TakenTable> dayEntries = new ArrayList<>();
                for (TakenTable tt: takenList) {
                    if (LocalDate.parse(tt.getDate()).equals(current)) {
                        dayEntries.add(tt);
                    }
                }

                int takenCount = 0;
                for (TakenTable tt : dayEntries) {
                    if (tt.isTaken()) {
                        takenCount++;
                    }
                }

                DayStatus status;
                if (totalMeds == 0) { //No meds
                    status = DayStatus.NO_DATA;
                } else if (dayEntries.isEmpty()) { //No entries
                    status = DayStatus.NO_DATA;
                } else if (takenCount == 0) { //red
                    status = DayStatus.NONE;
                } else if (takenCount < totalMeds) { //yellow
                    status = DayStatus.PARTIAL;
                } else { // green
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

    public boolean hasNoEntriesForDate(LocalDate date) {
        return takenTableDao.getTakenMapForDate(date.toString()).isEmpty();
    }

    public LiveData<List<DailyMedicationStatus>> getDailyMedicineStatus(LocalDate date) {

        MediatorLiveData<List<DailyMedicationStatus>> result = new MediatorLiveData<>();

        LiveData<List<Medicine>> medicinesLive = medicineDao.getAllMedicines();

        LiveData<List<TakenTable>> takenLive =takenTableDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<Medicine> meds = medicinesLive.getValue();
            List<TakenTable> taken = takenLive.getValue();

            if (meds == null || taken == null) return;

            Map<Integer, Boolean> takenMap = new HashMap<>();
            for (TakenTable t : taken) {
                takenMap.put(t.getMedicineId(), t.isTaken());
            }

            List<DailyMedicationStatus> list = new ArrayList<>();
            for (Medicine med : meds) {
                boolean isTaken = takenMap.getOrDefault(med.getId(), false);
                list.add(new DailyMedicationStatus(med, isTaken, date));
            }

            result.setValue(list);
        };

        result.addSource(medicinesLive, m -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());

        return result;
    }


}

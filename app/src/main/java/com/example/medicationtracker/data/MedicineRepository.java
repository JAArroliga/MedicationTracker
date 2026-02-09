package com.example.medicationtracker.data;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.DoseTaken;
import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.TakenTable;
import com.example.medicationtracker.ui.calendar.DailyMedicationStatus;
import com.example.medicationtracker.ui.medicine.MedicineWithDoses;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineRepository {

    private final MedicineDao medicineDao;
    private final DoseDao doseDao;
    private final DoseTakenDao doseTakenDao;
    private final TakenTableDao takenTableDao;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MedicineRepository(Application application) {
        MedicineDatabase database = MedicineDatabase.getInstance(application);
        medicineDao = database.medicineDao();
        doseDao = database.doseDao();
        doseTakenDao = database.doseTakenDao();
        takenTableDao = database.takenTableDao();
    }

    public void insertMedicineWithDoses(Medicine medicine, List<String> times) {
        executor.execute(() -> {
            long medicineId = medicineDao.insert(medicine);
            for (String time : times) {
                doseDao.insert(new Dose((int) medicineId, time));
            }
        });
    }

    public void updateMedicineWithDoses(Medicine medicine, List<String> times) {
        executor.execute(() -> {
            medicineDao.update(medicine);
            doseDao.deleteForMedicine(medicine.getId());
            for (String time : times) {
                doseDao.insert(new Dose(medicine.getId(), time));
            }
        });
    }

    public void delete(Medicine medicine) {
        executor.execute(() -> {
            doseDao.deleteForMedicine(medicine.getId());
            medicineDao.delete(medicine);
        });
    }

    public LiveData<List<MedicineWithDoses>> getMedicinesWithDoses() {
        return medicineDao.getMedicinesWithDoses();
    }

    public LiveData<List<DailyMedicationStatus>> getDailyMedicineStatus(LocalDate date) {
        MediatorLiveData<List<DailyMedicationStatus>> result = new MediatorLiveData<>();
        LiveData<List<Medicine>> medicinesLive = medicineDao.getAllMedicines();
        LiveData<List<TakenTable>> takenLive = takenTableDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<Medicine> medicines = medicinesLive.getValue();
            List<TakenTable> taken = takenLive.getValue();
            if (medicines == null || taken == null) return;

            Map<Integer, Boolean> takenMap = new HashMap<>();
            for (TakenTable t : taken) {
                takenMap.put(t.getMedicineId(), t.isTaken());
            }

            List<DailyMedicationStatus> list = new ArrayList<>();
            for (Medicine med : medicines) {
                boolean isTaken = takenMap.getOrDefault(med.getId(), false);
                list.add(new DailyMedicationStatus(med, isTaken, date));
            }
            result.setValue(list);
        };

        result.addSource(medicinesLive, m -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());
        return result;
    }

    public void markDoseTaken(int doseId, LocalDate date, DoseStatus status) {
        executor.execute(() -> {
            DoseTaken dt = new DoseTaken(doseId, date.toString(), status);
            doseTakenDao.insert(dt);
        });
    }

    public LiveData<List<DailyDoseStatus>> getDailyDoseStatus(LocalDate date) {
        MediatorLiveData<List<DailyDoseStatus>> result = new MediatorLiveData<>();

        LiveData<List<MedicineWithDoses>> medicinesLive =
                medicineDao.getMedicinesWithDoses();
        LiveData<List<DoseTaken>> takenLive =
                doseTakenDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<MedicineWithDoses> medicines = medicinesLive.getValue();
            List<DoseTaken> taken = takenLive.getValue();

            if (medicines == null) return;

            Map<Integer, DoseStatus> takenMap = new HashMap<>();
            if (taken != null) {
                for (DoseTaken dt : taken) {
                    takenMap.put(dt.getDoseId(), dt.getStatus());
                }
            }

            List<DailyDoseStatus> list = new ArrayList<>();

            for (MedicineWithDoses mwd : medicines) {
                Medicine med = mwd.medicine;

                for (Dose dose : mwd.doses) {
                    DoseStatus status =
                            takenMap.getOrDefault(dose.getId(), DoseStatus.PENDING);

                    list.add(new DailyDoseStatus(
                            med,
                            dose,
                            status == DoseStatus.TAKEN
                    ));
                }
            }

            result.setValue(list);
        };

        result.addSource(medicinesLive, m -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());

        return result;
    }

    public LiveData<List<Dose>> getAllExpectedDoses() {
        return doseDao.getAllDosesLive();
    }

    public LiveData<List<Medicine>> getMissedMedicine(LocalDate date) {
        MediatorLiveData<List<Medicine>> result = new MediatorLiveData<>();

        LiveData<List<Medicine>> allMedicinesLive = medicineDao.getAllMedicines();
        LiveData<List<TakenTable>> takenLive = takenTableDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<Medicine> allMedicines = allMedicinesLive.getValue();
            List<TakenTable> takenEntries = takenLive.getValue();

            if (allMedicines == null || takenEntries == null) return;

            Map<Integer, Boolean> takenMap = new HashMap<>();
            for (TakenTable t : takenEntries) {
                takenMap.put(t.getMedicineId(), t.isTaken());
            }

            List<Medicine> missed = new ArrayList<>();
            for (Medicine med : allMedicines) {
                Boolean wasTaken = takenMap.get(med.getId());
                if (wasTaken == null || !wasTaken) missed.add(med);
            }

            result.setValue(missed);
        };

        result.addSource(allMedicinesLive, meds -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());

        return result;
    }


    public LiveData<Boolean> hasNoEntriesForDate(LocalDate date) {
        return Transformations.map(takenTableDao.getTakenForDateLive(date.toString()), List::isEmpty);
    }

    public LiveData<Map<LocalDate, DayStatus>> getMedicationStatusMap(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        MediatorLiveData<Map<LocalDate, DayStatus>> result = new MediatorLiveData<>();

        LiveData<List<Dose>> allDosesLive = doseDao.getAllDosesLive();
        LiveData<List<DoseTaken>> takenListLive = doseTakenDao.getTakenForDateRangeLive(start.toString(), end.toString());

        Runnable recompute = () -> {
            List<Dose> allDoses = allDosesLive.getValue();
            List<DoseTaken> takenList = takenListLive.getValue();

            if (allDoses == null || takenList == null) return;

            Map<LocalDate, DayStatus> map = new HashMap<>();
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                int expected = allDoses.size();
                int takenCount = 0;

                for (DoseTaken dt : takenList) {
                    if (LocalDate.parse(dt.getDate()).equals(date) && dt.getStatus() == DoseStatus.TAKEN) {
                        takenCount++;
                    }
                }

                DayStatus status;
                if (expected == 0) status = DayStatus.NO_DATA;
                else if (takenCount == 0) status = DayStatus.NONE;
                else if (takenCount < expected) status = DayStatus.PARTIAL;
                else status = DayStatus.ALL_TAKEN;

                map.put(date, status);
            }

            result.setValue(map);
        };

        result.addSource(allDosesLive, doses -> recompute.run());
        result.addSource(takenListLive, taken -> recompute.run());

        return result;
    }

}

package com.example.medicationtracker.data;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.DoseTaken;
import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.notifications.AlarmScheduler;
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

    // ----- DAOs & Executor -----
    private final MedicineDao medicineDao;
    private final DoseDao doseDao;
    private final DoseTakenDao doseTakenDao;
    private final TakenTableDao takenTableDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Application application;

    // ----- Constructor -----
    public MedicineRepository(Application application) {
        this.application = application;

        MedicineDatabase database = MedicineDatabase.getInstance(application);
        medicineDao = database.medicineDao();
        doseDao = database.doseDao();
        doseTakenDao = database.doseTakenDao();
        takenTableDao = database.takenTableDao();
    }


    // ----- CRUD Operations -----
    public void insertMedicineWithDoses(Medicine medicine, List<String> times) {
        executor.execute(() -> {
            long medicineId = medicineDao.insert(medicine);
            for (String time : times) {
                Dose dose = new Dose((int) medicineId, time);
                long doseId = doseDao.insert(dose);
                dose.setId((int) doseId);

                AlarmScheduler.scheduleNextDose(application, medicine, dose);
            }
        });
    }

    public void updateMedicineWithDoses(Medicine medicine, List<String> times) {
        executor.execute(() -> {
            List<Dose> existingDoses = doseDao.getDosesForMedicineSync(medicine.getId());
            for (Dose dose: existingDoses) {
                AlarmScheduler.cancelAlarm(application, dose.getId());
            }

            medicineDao.update(medicine);
            doseDao.deleteForMedicine(medicine.getId());

            for (String time : times) {
                Dose dose = new Dose(medicine.getId(), time);
                long doseId = doseDao.insert(dose);
                dose.setId((int) doseId);

                AlarmScheduler.scheduleNextDose(application, medicine, dose);
            }
        });
    }

    public void delete(Medicine medicine) {
        executor.execute(() -> {
            List<Dose> doses = doseDao.getDosesForMedicineSync(medicine.getId());
            for (Dose dose : doses) {
                AlarmScheduler.cancelAlarm(application, dose.getId());
            }

            doseDao.deleteForMedicine(medicine.getId());
            medicineDao.delete(medicine);
        });
    }

    public void markDoseTaken(int doseId, LocalDate date, DoseStatus status) {
        executor.execute(() -> {
            DoseTaken dt = new DoseTaken(doseId, date.toString(), status);
            doseTakenDao.insert(dt);
        });
    }


    // ----- Basic Queries -----
    public LiveData<List<MedicineWithDoses>> getMedicinesWithDoses() {
        return medicineDao.getMedicinesWithDoses();
    }

    public LiveData<List<DailyMedicationStatus>> getDailyMedicineStatus(LocalDate date) {
        MediatorLiveData<List<DailyMedicationStatus>> result = new MediatorLiveData<>();

        LiveData<List<MedicineWithDoses>> medicinesLive = medicineDao.getMedicinesWithDoses();
        LiveData<List<DoseTaken>> takenLive = doseTakenDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<MedicineWithDoses> medicines = medicinesLive.getValue();
            List<DoseTaken> taken = takenLive.getValue();
            if (medicines == null) return;

            Map<Integer, DoseStatus> takenMap = new HashMap<>();
            if (taken != null) {
                for (DoseTaken dt : taken) takenMap.put(dt.getDoseId(), dt.getStatus());
            }

            List<DailyMedicationStatus> list = new ArrayList<>();
            for (MedicineWithDoses mwd : medicines) {
                Medicine med = mwd.medicine;
                if (!med.appliesOn(date)) continue;

                int expected = mwd.doses.size();
                int takenCount = 0;

                for (Dose dose : mwd.doses) {
                    if (takenMap.getOrDefault(dose.getId(), DoseStatus.PENDING) == DoseStatus.TAKEN) {
                        takenCount++;
                    }
                }

                DayStatus status = (takenCount == 0) ? DayStatus.NONE
                        : (takenCount < expected) ? DayStatus.PARTIAL
                        : DayStatus.ALL_TAKEN;

                list.add(new DailyMedicationStatus(med, status, date));
            }

            result.setValue(list);
        };

        result.addSource(medicinesLive, m -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());

        return result;
    }

    public LiveData<List<DailyDoseStatus>> getDailyDoseStatus(LocalDate date) {
        MediatorLiveData<List<DailyDoseStatus>> result = new MediatorLiveData<>();

        LiveData<List<MedicineWithDoses>> medicinesLive = medicineDao.getMedicinesWithDoses();
        LiveData<List<DoseTaken>> takenLive = doseTakenDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<MedicineWithDoses> medicines = medicinesLive.getValue();
            List<DoseTaken> taken = takenLive.getValue();
            if (medicines == null) return;

            Map<Integer, DoseStatus> takenMap = new HashMap<>();
            if (taken != null) {
                for (DoseTaken dt : taken) takenMap.put(dt.getDoseId(), dt.getStatus());
            }

            List<DailyDoseStatus> list = new ArrayList<>();
            for (MedicineWithDoses mwd : medicines) {
                Medicine med = mwd.medicine;
                if (!med.appliesOn(date)) continue;

                for (Dose dose : mwd.doses) {
                    boolean isTaken = takenMap.getOrDefault(dose.getId(), DoseStatus.PENDING) == DoseStatus.TAKEN;
                    list.add(new DailyDoseStatus(med, dose, isTaken));
                }
            }

            result.setValue(list);
        };

        result.addSource(medicinesLive, m -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());

        return result;
    }

    public LiveData<Integer> getExpectedDoseCountForDate(LocalDate date) {
        MediatorLiveData<Integer> result = new MediatorLiveData<>();
        LiveData<List<MedicineWithDoses>> medsLive = medicineDao.getMedicinesWithDoses();

        Runnable recompute = () -> {
            List<MedicineWithDoses> meds = medsLive.getValue();
            if (meds == null) return;

            int count = 0;
            for (MedicineWithDoses mwd : meds) {
                if (mwd.medicine.appliesOn(date)) count += mwd.doses.size();
            }

            result.setValue(count);
        };

        result.addSource(medsLive, m -> recompute.run());
        return result;
    }

    public LiveData<List<Medicine>> getMissedMedicine(LocalDate date) {
        MediatorLiveData<List<Medicine>> result = new MediatorLiveData<>();

        LiveData<List<MedicineWithDoses>> medsLive = medicineDao.getMedicinesWithDoses();
        LiveData<List<DoseTaken>> takenLive = doseTakenDao.getTakenForDateLive(date.toString());

        Runnable recompute = () -> {
            List<MedicineWithDoses> meds = medsLive.getValue();
            List<DoseTaken> taken = takenLive.getValue();
            if (meds == null) return;

            Map<Integer, DoseStatus> takenMap = new HashMap<>();
            if (taken != null) for (DoseTaken dt : taken) takenMap.put(dt.getDoseId(), dt.getStatus());

            List<Medicine> missed = new ArrayList<>();
            for (MedicineWithDoses mwd : meds) {
                Medicine med = mwd.medicine;
                if (!med.appliesOn(date)) continue;

                boolean anyMissed = false;
                for (Dose dose : mwd.doses) {
                    if (takenMap.getOrDefault(dose.getId(), DoseStatus.PENDING) != DoseStatus.TAKEN) {
                        anyMissed = true;
                        break;
                    }
                }

                if (anyMissed && !mwd.doses.isEmpty()) missed.add(med);
            }

            result.setValue(missed);
        };

        result.addSource(medsLive, m -> recompute.run());
        result.addSource(takenLive, t -> recompute.run());

        return result;
    }

    public LiveData<Boolean> hasNoEntriesForDate(LocalDate date) {
        MediatorLiveData<Boolean> result = new MediatorLiveData<>();
        LiveData<List<MedicineWithDoses>> medsLive = medicineDao.getMedicinesWithDoses();

        Runnable recompute = () -> {
            List<MedicineWithDoses> meds = medsLive.getValue();
            if (meds == null) return;

            boolean hasAnyDoses = false;
            for (MedicineWithDoses mwd : meds) {
                if (!mwd.medicine.appliesOn(date)) continue;
                if (!mwd.doses.isEmpty()) {
                    hasAnyDoses = true;
                    break;
                }
            }

            result.setValue(!hasAnyDoses);
        };

        result.addSource(medsLive, m -> recompute.run());
        return result;
    }


    // ----- Month / Calendar Mapping -----
    public LiveData<Map<LocalDate, DayStatus>> getMedicationStatusMap(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        MediatorLiveData<Map<LocalDate, DayStatus>> result = new MediatorLiveData<>();
        LiveData<List<MedicineWithDoses>> medsLive = medicineDao.getMedicinesWithDoses();
        LiveData<List<DoseTaken>> takenListLive = doseTakenDao.getTakenForDateRangeLive(start.toString(), end.toString());

        Runnable recompute = () -> {
            List<MedicineWithDoses> meds = medsLive.getValue();
            List<DoseTaken> takenList = takenListLive.getValue();
            if (meds == null || takenList == null) return;

            Map<LocalDate, Integer> takenPerDay = new HashMap<>();
            for (DoseTaken dt : takenList) {
                if (dt.getStatus() == DoseStatus.TAKEN) {
                    LocalDate d = LocalDate.parse(dt.getDate());
                    takenPerDay.merge(d, 1, Integer::sum);
                }
            }

            Map<LocalDate, DayStatus> map = new HashMap<>();
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                int expected = 0;
                for (MedicineWithDoses mwd : meds) if (mwd.medicine.appliesOn(date)) expected += mwd.doses.size();

                int takenCount = takenPerDay.getOrDefault(date, 0);

                DayStatus status = (expected == 0) ? DayStatus.NO_DATA
                        : (takenCount == 0) ? DayStatus.NONE
                        : (takenCount < expected) ? DayStatus.PARTIAL
                        : DayStatus.ALL_TAKEN;

                map.put(date, status);
            }

            result.setValue(map);
        };

        result.addSource(medsLive, meds -> recompute.run());
        result.addSource(takenListLive, taken -> recompute.run());

        return result;
    }


    // ----- Synchronous Helpers -----
    public List<DailyDoseStatus> getDailyDoseStatusSync(LocalDate date) {
        List<MedicineWithDoses> medicines = medicineDao.getMedicinesWithDosesSync();
        List<DoseTaken> taken = doseTakenDao.getTakenForDateSync(date.toString());

        if (medicines == null) return List.of();

        Map<Integer, DoseStatus> takenMap = new HashMap<>();
        if (taken != null) for (DoseTaken dt : taken) takenMap.put(dt.getDoseId(), dt.getStatus());

        List<DailyDoseStatus> list = new ArrayList<>();
        for (MedicineWithDoses mwd : medicines) {
            Medicine med = mwd.medicine;
            if (!med.appliesOn(date)) continue;

            for (Dose dose : mwd.doses) {
                boolean isTaken = takenMap.getOrDefault(dose.getId(), DoseStatus.PENDING) == DoseStatus.TAKEN;
                list.add(new DailyDoseStatus(med, dose, isTaken));
            }
        }

        return list;
    }

    // ---- Alarm Methods ----
    public void scheduleAllAlarms(Context context) {
        executor.execute(() -> {
            List<MedicineWithDoses> medicines = medicineDao.getMedicinesWithDosesSync();
            if (medicines == null) return;

            for (MedicineWithDoses mwd : medicines) {
                for (Dose dose : mwd.doses) {
                    AlarmScheduler.scheduleNextDose(context, mwd.medicine, dose);
                }
            }
        });
    }
}

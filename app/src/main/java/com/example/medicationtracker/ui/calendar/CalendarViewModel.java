package com.example.medicationtracker.ui.calendar;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.DayStatus;
import com.example.medicationtracker.data.MedicineRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CalendarViewModel extends AndroidViewModel {

    private final MutableLiveData<Map<LocalDate, DayStatus>> medicationStatusMap = new MutableLiveData<>(new HashMap<>());
    private MedicineRepository medicineRepository;
    private Executor executor = Executors.newSingleThreadExecutor();


    public CalendarViewModel(@NonNull Application application) {
        super(application);
        medicineRepository = new MedicineRepository(application);
    }

    public void loadMonthStatus(YearMonth month) {
        medicineRepository.getMedicationStatusMap(month).observeForever(map -> monthStatusMap.postValue(map));
    }

    public LiveData<Map<LocalDate, DayStatus>> getMonthMedicationStatus(YearMonth month) {
        return medicineRepository.getMedicationStatusMap(month);
    }

    public LiveData<String> getDaySummary(LocalDate date) {
        MutableLiveData<String> summaryLiveData = new MutableLiveData<>();

        executor.execute(() -> {
            Map<LocalDate, DayStatus> map = medicationStatusMap.getValue();
            DayStatus status = (map != null) ? map.get(date) : null;
            String summary;

            if (status == null) {
                summary = "No data for " + date;
            } else if (status == DayStatus.ALL_TAKEN) {
                summary = "All medications taken";
            } else {
                List<Medicine> missedMedicines = medicineRepository.getMissedMedicine(date);

                if (missedMedicines.isEmpty()) {
                    summary = "All medications taken";
                } else {
                    StringBuilder sb = new StringBuilder("Missed medications: ");
                    for (Medicine med : missedMedicines) {
                        sb.append(med.getName()).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());
                    summary = sb.toString();
                }
            }
            summaryLiveData.postValue(summary);
        });

        return summaryLiveData;
    }

}
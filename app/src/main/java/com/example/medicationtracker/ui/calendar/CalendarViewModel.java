package com.example.medicationtracker.ui.calendar;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.lifecycle.Transformations;

import com.example.medicationtracker.data.DailyDoseStatus;
import com.example.medicationtracker.data.DayStatus;
import com.example.medicationtracker.data.MedicineRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class CalendarViewModel extends AndroidViewModel {

    private final MedicineRepository medicineRepository;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private final LiveData<List<DailyDoseStatus>> dailyDoses;
    private final LiveData<Boolean> hasNoEntries;
    private final MutableLiveData<YearMonth> selectedMonth = new MutableLiveData<>();
    private final LiveData<Map<LocalDate, DayStatus>> monthStatus;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        medicineRepository = new MedicineRepository(application);

        dailyDoses = Transformations.switchMap(selectedDate, date -> {
            if (date == null) {
                return new MutableLiveData<>(List.of());
            }
            return medicineRepository.getDailyDoseStatus(date);
        });

        hasNoEntries = Transformations.switchMap(selectedDate, date -> {
            if (date == null) return new MutableLiveData<>(true);
            return medicineRepository.hasNoEntriesForDate(date);
        });

        monthStatus = Transformations.switchMap(selectedMonth, medicineRepository::getMedicationStatusMap);

        selectedDate.setValue(LocalDate.now());
        selectedMonth.setValue(YearMonth.now());
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public LiveData<List<DailyDoseStatus>> getDailyDoses() {
        return dailyDoses;
    }

    public void setSelectedMonth(YearMonth month) {
        selectedMonth.setValue(month);
    }

    public LiveData<Map<LocalDate, DayStatus>> getMonthStatus() {
        return monthStatus;
    }

    public LiveData<Boolean> getHasNoEntries() {
        return hasNoEntries;
    }

    public List<DailyDoseStatus> getDailyDosesForDate(LocalDate date) {
        if (date == null) return List.of();
        List<DailyDoseStatus> doses = medicineRepository.getDailyDoseStatusSync(date);
        return doses != null ? doses : List.of();
    }

    public List<DailyDoseStatus> getDailyDosesSync(LocalDate date) {
        return medicineRepository.getDailyDoseStatusSync(date);
    }

    public LocalDate getSelectedDateValue() {
        return selectedDate.getValue() != null ? selectedDate.getValue() : LocalDate.now();
    }

}
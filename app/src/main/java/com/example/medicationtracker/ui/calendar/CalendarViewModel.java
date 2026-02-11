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

    private MedicineRepository medicineRepository;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private final LiveData<List<DailyDoseStatus>> dailyDoses = Transformations.switchMap(selectedDate, date -> {
        if (date == null) {
            return new MutableLiveData<>(List.of());
        }
        return medicineRepository.getDailyDoseStatus(date);
    });

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        medicineRepository = new MedicineRepository(application);
        selectedDate.setValue(LocalDate.now());
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public LiveData<List<DailyDoseStatus>> getDailyDoses() {
        return dailyDoses;
    }

    private final MutableLiveData<YearMonth> selectedMonth = new MutableLiveData<>();

    private final LiveData<Map<LocalDate, DayStatus>> monthStatus = Transformations.switchMap(selectedMonth, medicineRepository::getMedicationStatusMap);

    public void setSelectedMonth(YearMonth month) {
        selectedMonth.setValue(month);
    }

    public LiveData<Map<LocalDate, DayStatus>> getMonthStatus() {
        return monthStatus;
    }


}
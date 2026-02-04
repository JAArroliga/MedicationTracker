package com.example.medicationtracker.ui.calendar;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.lifecycle.Transformations;

import com.example.medicationtracker.data.DayStatus;
import com.example.medicationtracker.data.MedicineRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class CalendarViewModel extends AndroidViewModel {

    private MedicineRepository medicineRepository;
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();

    private final LiveData<List<DailyMedicationStatus>> dailyMedicines =
            Transformations.switchMap(selectedDate, date -> {
                if (date == null) {
                    return new MutableLiveData<>(List.of());
                }
                return medicineRepository.getDailyMedicineStatus(date);
            });

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        medicineRepository = new MedicineRepository(application);
    }

    public void setSelectedDate(LocalDate date) {
        selectedDate.setValue(date);
    }

    public LiveData<Map<LocalDate, DayStatus>> getMonthMedicationStatus(YearMonth month) {
        return medicineRepository.getMedicationStatusMap(month);
    }

    public LiveData<List<DailyMedicationStatus>> getDailyMedicines() {
        return dailyMedicines;
    }

}
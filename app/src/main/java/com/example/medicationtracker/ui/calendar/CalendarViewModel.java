package com.example.medicationtracker.ui.calendar;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

import com.example.medicationtracker.data.DayStatus;
import com.example.medicationtracker.data.MedicineRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class CalendarViewModel extends AndroidViewModel {

    private final MutableLiveData<Map<LocalDate, DayStatus>> medicationStatusMap = new MutableLiveData<>(new HashMap<>());
    private MedicineRepository medicineRepository;

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        medicineRepository = new MedicineRepository(application);
    }

    private LiveData<Map<LocalDate, DayStatus>> getMonthMedicationStatus (YearMonth month) {
        return medicineRepository.getMedicationStatusMap(month);
    }

}
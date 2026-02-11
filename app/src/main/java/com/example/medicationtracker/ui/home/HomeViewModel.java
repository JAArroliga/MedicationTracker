package com.example.medicationtracker.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.DailyDoseStatus;
import com.example.medicationtracker.data.DoseStatus;
import com.example.medicationtracker.data.MedicineRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    private final MedicineRepository repository;
    private final LiveData<List<DailyDoseStatus>> todayDoses;
    private final LiveData<Boolean> hasNoDosesToday;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
        todayDoses = repository.getDailyDoseStatus(LocalDate.now());
        hasNoDosesToday = repository.hasNoEntriesForDate(LocalDate.now());
    }

    public LiveData<List<DailyDoseStatus>> getTodayDoses() {
        return todayDoses;
    }

    public LiveData<Boolean> getHasNoDosesToday() {
        return hasNoDosesToday;
    }

    public void markTaken(DailyDoseStatus dose) {
        repository.markDoseTaken(
                dose.getDose().getId(),
                LocalDate.now(),
                DoseStatus.TAKEN
        );
    }

    public void undoTaken(DailyDoseStatus dose) {
        repository.markDoseTaken(
                dose.getDose().getId(),
                LocalDate.now(),
                DoseStatus.PENDING
        );
    }
}

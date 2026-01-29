package com.example.medicationtracker.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<Boolean> medicineTaken = new MutableLiveData<>(false);

    public LiveData<Boolean> getMedicineTaken() {
        return medicineTaken;
    }

    public void markTaken() {
        medicineTaken.setValue(true);
    }
}
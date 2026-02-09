package com.example.medicationtracker.data;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.Medicine;

public class DailyDoseStatus {

    private final Medicine medicine;
    private final Dose dose;
    private final boolean taken;

    public DailyDoseStatus(Medicine medicine, Dose dose, boolean taken) {
        this.medicine = medicine;
        this.dose = dose;
        this.taken = taken;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public Dose getDose() {
        return dose;
    }

    public boolean isTaken() {
        return taken;
    }
}


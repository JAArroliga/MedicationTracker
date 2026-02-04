package com.example.medicationtracker.ui.calendar;

import com.example.medicationtracker.Medicine;

import java.time.LocalDate;

public class DailyMedicationStatus {

    private final Medicine medicine;
    private final boolean taken;
    private final LocalDate date;

    public DailyMedicationStatus(Medicine medicine, boolean taken, LocalDate date) {
        this.medicine = medicine;
        this.taken = taken;
        this.date = date;

    }

    public Medicine getMedicine() {
        return medicine;
    }

    public boolean isTaken() {
        return taken;
    }

    public LocalDate getDate() {
        return date;
    }
}

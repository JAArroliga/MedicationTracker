package com.example.medicationtracker.ui.calendar;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.DayStatus;

import java.time.LocalDate;

public class DailyMedicationStatus {

    private final Medicine medicine;
    private final DayStatus status;
    private final LocalDate date;

    public DailyMedicationStatus(Medicine medicine, DayStatus status, LocalDate date) {
        this.medicine = medicine;
        this.status = status;
        this.date = date;

    }

    public boolean isAllTaken() {
        return status == DayStatus.ALL_TAKEN;
    }

    public boolean isPartial() {
        return status == DayStatus.PARTIAL;
    }

    public boolean isNoneTaken() {
        return status == DayStatus.NONE;
    }

}

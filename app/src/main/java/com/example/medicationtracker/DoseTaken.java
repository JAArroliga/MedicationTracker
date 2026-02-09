package com.example.medicationtracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.example.medicationtracker.data.DoseStatus;

@Entity(tableName = "dose_taken", primaryKeys = {"doseId", "date"})
public class DoseTaken {

    private int doseId;
    @NonNull
    private String date;
    @NonNull
    private DoseStatus status;

    public DoseTaken(int doseId, @NonNull String date, @NonNull DoseStatus status) {
        this.doseId = doseId;
        this.date = date;
        this.status = status;
    }

    public int getDoseId() {
        return doseId;
    }

    public void setDoseId(int doseId) {
        this.doseId = doseId;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    @NonNull
    public DoseStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull DoseStatus status) {
        this.status = status;
    }


}

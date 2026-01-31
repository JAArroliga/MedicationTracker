package com.example.medicationtracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "taken_table", primaryKeys = {"medicineId", "date"})
public class TakenTable {

    private int medicineId;
    @NonNull
    private String date;
    private boolean taken;

    public TakenTable(int medicineId, String date, boolean taken) {
        this.medicineId = medicineId;
        this.date = date;
        this.taken = taken;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public String getDate() {
        return date;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setMedicineId(int id) {
        this.medicineId = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }
}

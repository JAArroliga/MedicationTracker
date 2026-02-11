package com.example.medicationtracker;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Represents a time-of-day for a medicine.
 * A Dose does NOT imply a specific date or day of week.
 * Applicability is determined by the parent Medicine.
 */
@Entity(tableName = "dose_table", foreignKeys = @ForeignKey(entity = Medicine.class, parentColumns = "id", childColumns = "medicineId", onDelete = ForeignKey.CASCADE))
public class Dose {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int medicineId;
    private String time;

    public Dose(int medicineId, String time) {
        this.medicineId = medicineId;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}

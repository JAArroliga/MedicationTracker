package com.example.medicationtracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicine_table")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double dosageAmount;
    private String dosageUnit;
    private String time;

    public Medicine(int id, String name, double dosageAmount, String dosageUnit, String time) {
        this.id = id;
        this.name = name;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.time = time;

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDosageAmount() {
        return dosageAmount;
    }

    public String getDosageUnit() {
        return dosageUnit;
    }

    public String getTime() {
        return time;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDosageAmount(double dosageAmount) {
        this.dosageAmount = dosageAmount;
    }

    public void setDosageUnit(String dosageUnit) {
        this.dosageUnit = dosageUnit;
    }


    public void setTime(String time){
        this.time = time;
    }

    public String getFormattedDosage() {
        return dosageAmount + " " + dosageUnit;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " - " + getFormattedDosage() + " - " + time;
    }
}

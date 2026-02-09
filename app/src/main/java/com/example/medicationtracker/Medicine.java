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

    private String type; // "pill" or "syringe"
    private String frequency; // "daily", "weekly", etc.

    public Medicine(int id, String name, double dosageAmount, String dosageUnit, String type, String frequency) {
        this.id = id;
        this.name = name;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.type = type;
        this.frequency = frequency;

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

    public String getType() {
        return type;
    }

    public String getFrequency() {
        return frequency;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }


    public String getFormattedDosage() {
        return dosageAmount + " " + dosageUnit;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " - " + getFormattedDosage() + " - " + type + " - " + frequency;
    }
}

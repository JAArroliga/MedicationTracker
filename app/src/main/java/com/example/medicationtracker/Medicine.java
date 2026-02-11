package com.example.medicationtracker;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity(tableName = "medicine_table")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private double dosageAmount;
    private String dosageUnit;
    private String type; // "pill" or "syringe"
    private String frequency; // UI hint only (actual schedule is defined by daysOfWeekMask + doses)
    @ColumnInfo(name = "days_of_week_mask")
    private int daysOfWeekMask = 127;

    public Medicine(int id, String name, double dosageAmount, String dosageUnit, String type, String frequency, int daysOfWeekMask) {
        this.id = id;
        this.name = name;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.type = type;
        this.frequency = frequency;
        this.daysOfWeekMask = daysOfWeekMask;
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

    public int getDaysOfWeekMask() {
        return daysOfWeekMask;
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

    public void setDaysOfWeekMask(int daysOfWeekMask) {
        this.daysOfWeekMask = daysOfWeekMask;
    }

    public String getFormattedDosage() {
        return dosageAmount + " " + dosageUnit;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " - " + getFormattedDosage() + " - " + type;
    }

    public boolean appliesOn(LocalDate date) {
        return (daysOfWeekMask & bitForDay(date.getDayOfWeek())) != 0;
    }

    public void setDayEnabled(DayOfWeek day, boolean enabled) {
        int bit = bitForDay(day);
        if (enabled) {
            daysOfWeekMask |= bit;
        } else {
            daysOfWeekMask &= ~bit;
        }
    }

    private static int bitForDay(DayOfWeek day) {
        return 1 << (day.getValue() % 7);
    }
}

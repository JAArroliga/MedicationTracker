package com.example.medicationtracker;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicine_table")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String dosage;
    private String time;

    public Medicine(String name, String dosage, String time) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getTime() {
        return time;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String dosage){
        this.name = name;
    }

    public void setDosage(String dosage){
        this.dosage = dosage;
    }

    public void setTime(String time){
        this.time = time;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " - " + dosage + " - " + time;
    }
}

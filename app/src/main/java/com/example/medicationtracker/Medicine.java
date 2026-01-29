package com.example.medicationtracker;

public class Medicine {
    private String name;
    private String dosage;
    private String frequency;

    public Medicine(String name, String dosage, String frequency) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;

    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String toString() {
        return name + " - " + dosage + " - " + frequency;
    }

    public void setName(){
        this.name = name;
    }

    public void setDosage(){
        this.dosage = dosage;
    }

    public void setFrequency(){
        this.frequency = frequency;
    }
}

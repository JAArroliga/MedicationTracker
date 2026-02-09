package com.example.medicationtracker.ui.medicine;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.Medicine;

import java.util.List;

public class MedicineWithDoses {
    @Embedded
    public final Medicine medicine;
    @Relation(
            parentColumn = "id",
            entityColumn = "medicineId"
    )
    public final List<Dose> doses;

    public MedicineWithDoses(Medicine medicine, List<Dose> doses) {
        this.medicine = medicine;
        this.doses = doses;
    }
}


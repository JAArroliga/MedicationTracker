package com.example.medicationtracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.ui.medicine.MedicineWithDoses;

import java.util.List;

@Dao
public interface MedicineDao {

    @Insert
    long insert(Medicine medicine);

    @Delete
    void delete(Medicine medicine);

    @Query("DELETE FROM medicine_table")
    void deleteAll();

    @Query("SELECT * FROM medicine_table")
    LiveData<List<Medicine>> getAllMedicines();

    @Query("SELECT * FROM medicine_table")
    LiveData<List<Medicine>> getAllMedicinesList();

    @Update
    void update(Medicine medicine);

    @Transaction
    @Query("SELECT * FROM medicine_table")
    LiveData<List<MedicineWithDoses>> getMedicinesWithDoses();

    @Query("SELECT * FROM medicine_table")
    List<MedicineWithDoses> getMedicinesWithDosesSync();



}

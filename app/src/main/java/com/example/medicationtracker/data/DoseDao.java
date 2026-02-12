package com.example.medicationtracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationtracker.Dose;

import java.util.List;

@Dao
public interface DoseDao {

    @Insert
    long insert(Dose dose);

    @Update
    void update(Dose dose);

    @Delete
    void delete(Dose dose);

    @Query("SELECT * FROM dose_table WHERE medicineId = :medicineId")
    LiveData<List<Dose>> getDosesForMedicine(int medicineId);

    @Query("SELECT * FROM dose_table")
    LiveData<List<Dose>> getAllDosesLive();

    @Query("DELETE FROM dose_table WHERE medicineId = :medicineId")
    void deleteForMedicine(int medicineId);

    @Query("SELECT * FROM dose_table WHERE medicineId = :medicineId")
    List<Dose> getDosesForMedicineSync(int medicineId);

    @Query("SELECT * FROM dose_table WHERE id = :id")
    Dose getDoseById(int id);

    @Query("SELECT * FROM dose_table")
    List<Dose> getAllDosesSync();


}

package com.example.medicationtracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationtracker.Medicine;

import java.util.List;

@Dao
public interface MedicineDao {

    @Insert
    void insert(Medicine medicine);

    @Delete
    void delete(Medicine medicine);

    @Query("DELETE FROM medicine_table")
    void deleteAll();

    @Query("SELECT * FROM medicine_table ORDER BY time ASC")
    LiveData<List<Medicine>> getAllMedicines();

    @Update
    void update(Medicine medicine);

    @Query("SELECT * FROM medicine_table")
    List<Medicine> getAllMedicinesList();
}

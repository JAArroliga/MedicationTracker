package com.example.medicationtracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationtracker.TakenTable;

import java.util.List;

@Dao
public interface TakenTableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TakenTable takenTable);

    @Update
    void update(TakenTable takenTable);

    @Delete
    void delete(TakenTable takenTable);

    @Query("SELECT * FROM taken_table WHERE medicineId = :medicineId AND date = :date LIMIT 1")
    TakenTable getTakenTable(int medicineId, String date);

    @Query("SELECT * FROM taken_table WHERE date = :date")
    List<TakenTable> getTakenMapForDate(String date);

    @Query("SELECT * FROM taken_table WHERE date BETWEEN :startDate AND :endDate")
    List<TakenTable> getTakenMapForDateRange(String startDate, String endDate);

    @Query("SELECT * FROM taken_table WHERE date = :date")
    LiveData<List<TakenTable>> getTakenForDateLive(String date);

}

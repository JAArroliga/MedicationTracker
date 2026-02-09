package com.example.medicationtracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationtracker.DoseTaken;

import java.util.List;

@Dao
public interface DoseTakenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DoseTaken doseTaken);

    @Update
    void update(DoseTaken doseTaken);

    @Delete
    void delete(DoseTaken doseTaken);

    @Query("SELECT * FROM dose_taken WHERE doseId = :doseId AND date = :date")
    DoseTaken getDoseTaken(int doseId, String date);

    @Query("SELECT * FROM dose_taken WHERE date = :date")
    LiveData<List<DoseTaken>> getTakenForDateLive(String date);

    @Query("SELECT * FROM dose_taken WHERE date BETWEEN :startDate AND :endDate")
    List<DoseTaken> getTakenForDateRange(String startDate, String endDate);

    @Query("SELECT * FROM dose_taken WHERE date BETWEEN :startDate AND :endDate")
    LiveData<List<DoseTaken>> getTakenForDateRangeLive(String startDate, String endDate);


}

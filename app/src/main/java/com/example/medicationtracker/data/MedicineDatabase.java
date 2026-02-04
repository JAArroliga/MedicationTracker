package com.example.medicationtracker.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.TakenTable;

@Database(entities = {Medicine.class, TakenTable.class}, version = 2, exportSchema = false)
public abstract class MedicineDatabase extends RoomDatabase{

    private static MedicineDatabase instance;
    public abstract MedicineDao medicineDao();
    public abstract TakenTableDao takenTableDao();


    public static synchronized MedicineDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), MedicineDatabase.class, "medicine_database").fallbackToDestructiveMigration().build();
    }
        return instance;
        }

}

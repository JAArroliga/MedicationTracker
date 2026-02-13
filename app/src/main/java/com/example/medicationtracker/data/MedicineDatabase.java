package com.example.medicationtracker.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.medicationtracker.Converters;
import com.example.medicationtracker.Dose;
import com.example.medicationtracker.DoseTaken;
import com.example.medicationtracker.Medicine;

@Database(entities = {Medicine.class, Dose.class, DoseTaken.class}, version = 8, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MedicineDatabase extends RoomDatabase{

    private static MedicineDatabase instance;
    public abstract MedicineDao medicineDao();
    public abstract DoseDao doseDao();
    public abstract DoseTakenDao doseTakenDao();

    public static synchronized MedicineDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), MedicineDatabase.class, "medicine_database").fallbackToDestructiveMigration().build();
    }
        return instance;
        }

}

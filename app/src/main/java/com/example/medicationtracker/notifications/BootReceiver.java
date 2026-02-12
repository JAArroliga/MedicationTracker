package com.example.medicationtracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.data.MedicineDatabase;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            new Thread(() -> {
                MedicineDatabase db = MedicineDatabase.getInstance(context);
                List<Dose> doses = db.doseDao().getAllDosesSync();

                for (Dose dose : doses) {
                    AlarmScheduler.scheduleAlarm(context, dose);
                }
            }).start();
        }
    }
}

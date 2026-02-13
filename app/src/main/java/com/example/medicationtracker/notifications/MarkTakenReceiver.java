package com.example.medicationtracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.medicationtracker.data.MedicineRepository;

public class MarkTakenReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int doseId = intent.getIntExtra("doseId", -1);
        if (doseId == -1) return;

        Log.d("MarkTakenReceiver", "Mark taken clicked for doseId: " + doseId);

        MedicineRepository repository = new MedicineRepository((android.app.Application) context.getApplicationContext());

        repository.markDoseTaken(
                doseId,
                java.time.LocalDate.now(),
                com.example.medicationtracker.data.DoseStatus.TAKEN
        );

    }
}

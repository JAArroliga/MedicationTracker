package com.example.medicationtracker.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.medicationtracker.data.DoseStatus;
import com.example.medicationtracker.data.MedicineRepository;
import com.example.medicationtracker.util.DebugLogger;

import java.time.LocalDate;

public class MarkTakenReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int doseId = intent.getIntExtra("doseId", -1);
        if (doseId == -1) return;

        DebugLogger.log(context,
                "MARK_TAKEN_CLICKED | doseId=" + doseId);


        MedicineRepository repository = new MedicineRepository((android.app.Application) context.getApplicationContext());

        String dateString = intent.getStringExtra("doseDate");
        if (dateString == null) return;

        LocalDate doseDate = LocalDate.parse(dateString);

        repository.markDoseTaken(
                doseId,
                doseDate,
                DoseStatus.TAKEN
        );

        DebugLogger.log(context,
                "MARK_TAKEN_REPO_CALLED | doseId=" + doseId);

    }
}

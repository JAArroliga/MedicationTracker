package com.example.medicationtracker.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.MainActivity;
import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.R;
import com.example.medicationtracker.data.MedicineDatabase;


public class MedicationReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "medication_reminder_channel";
    public static final String EXTRA_DOSE_ID = "extra_dose_id";
    public static final String EXTRA_DOSE_MED_NAME = "extra_med_name";

    @Override
    public void onReceive(Context context, Intent intent) {

        int doseId = intent.getIntExtra(EXTRA_DOSE_ID, -1);
        String medName = intent.getStringExtra(EXTRA_DOSE_MED_NAME);

        if (medName == null) {
            medName = "Medication Reminder";
        }

        // 🔹 Open App Intent
        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent =
                PendingIntent.getActivity(
                        context,
                        doseId,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        // 🔹 Snooze Intent (DECLARE BEFORE USING)
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        snoozeIntent.putExtra("doseId", doseId);

        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        doseId + 1000, // avoid collision with main alarm
                        snoozeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        Intent markTakenIntent = new Intent(context, MarkTakenReceiver.class);
        markTakenIntent.putExtra("doseId", doseId);

        PendingIntent markTakenPendingIntent = PendingIntent.getBroadcast(context, doseId, markTakenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔹 Build Notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Time to take your medication")
                        .setContentText(medName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(openAppPendingIntent)
                        .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
                        .addAction(R.drawable.ic_mark_taken, "Mark Taken", markTakenPendingIntent)
                        .setAutoCancel(true);

        Log.d("ReminderReceiver", "Displaying notification for doseId: " + doseId);
        // 🔹 Show Notification
        NotificationManagerCompat.from(context).notify(doseId, builder.build());

        Log.d("ReminderReceiver", "ALARM FIRED for doseId: " + doseId +
                " at time: " + System.currentTimeMillis());

        // 🔹 Schedule next dose
        if (doseId != -1) {
            MedicineDatabase db = MedicineDatabase.getInstance(context);

            new Thread(() -> {
                Dose dose = db.doseDao().getDoseById(doseId);

                if (dose != null) {
                    Medicine medicine = db.medicineDao()
                            .getMedicineById(dose.getMedicineId());

                    if (medicine != null) {
                        AlarmScheduler.scheduleNextDose(context, medicine, dose);
                        Log.d("ReminderReceiver", "Next dose scheduled");
                    }
                }
            }).start();
        }


    }

}

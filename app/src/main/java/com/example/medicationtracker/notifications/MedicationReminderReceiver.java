package com.example.medicationtracker.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.medicationtracker.MainActivity;
import com.example.medicationtracker.R;

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

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, doseId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Time to take your medication")
                .setContentText(medName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(openAppPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManger = NotificationManagerCompat.from(context);
        notificationManger.notify(doseId, builder.build());
    }

}

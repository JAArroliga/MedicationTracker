package com.example.medicationtracker.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int doseId = intent.getIntExtra("doseId", -1);
        Log.d("SnoozeReceiver", "SNOOZE BUTTON PRESSED for doseId: " + doseId);

        if (doseId == -1) return;

        AlarmScheduler.cancelAlarm(context, doseId);
        Log.d("SnoozeReceiver", "Original alarm cancelled");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(doseId);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent newIntent = new Intent(context, MedicationReminderReceiver.class);
        newIntent.putExtra(MedicationReminderReceiver.EXTRA_DOSE_ID, doseId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, doseId, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);
        Log.d("SnoozeReceiver", "Scheduling snoozed alarm for: " + cal.getTime());

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(),
                    pendingIntent
            );
        }
    }
}

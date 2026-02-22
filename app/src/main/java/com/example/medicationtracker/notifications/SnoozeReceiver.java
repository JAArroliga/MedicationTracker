package com.example.medicationtracker.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.Medicine;
import com.example.medicationtracker.data.MedicineDatabase;
import com.example.medicationtracker.ui.settings.SettingsManager;
import com.example.medicationtracker.util.DebugLogger;

import java.util.Calendar;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int doseId = intent.getIntExtra("doseId", -1);
        DebugLogger.log(context,
                "SNOOZE_CLICKED | doseId=" + doseId);


        if (doseId == -1) return;

        AlarmScheduler.cancelAlarm(context, doseId);
        Log.d("SnoozeReceiver", "Original alarm cancelled");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(doseId);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        MedicineDatabase db = MedicineDatabase.getInstance(context);
        Dose dose = db.doseDao().getDoseById(doseId);

        if (dose == null) return;

        Medicine medicine = db.medicineDao().getMedicineById(dose.getMedicineId());

        if (medicine == null) return;

        Intent newIntent = new Intent(context, MedicationReminderReceiver.class);

        newIntent.putExtra(MedicationReminderReceiver.EXTRA_DOSE_ID, doseId);
        newIntent.putExtra(MedicationReminderReceiver.EXTRA_DOSE_MED_NAME, medicine.getName());

        String today = java.time.LocalDate.now().toString();
        newIntent.putExtra("doseDate", today);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, doseId, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        SettingsManager settings = new SettingsManager(context);
        int snoozeMinutes = settings.getSnoozeMinutes();
        long triggerAtMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
        );


        DebugLogger.log(context,
                "SNOOZE_RESCHEDULED | doseId=" + doseId +
                        " | minutes=" + snoozeMinutes);
    }
}

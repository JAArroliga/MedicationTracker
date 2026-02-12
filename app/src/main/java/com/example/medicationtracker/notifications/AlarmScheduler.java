package com.example.medicationtracker.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

import com.example.medicationtracker.Dose;
import com.example.medicationtracker.Medicine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler {

    public static void scheduleNextDose(Context context, Medicine medicine, Dose dose) {
        Calendar nextTrigger = calculateNextTrigger(medicine, dose);
        if (nextTrigger == null) return;

        Log.d("AlarmScheduler", "Scheduling alarm for: " + nextTrigger.getTime());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        intent.putExtra(MedicationReminderReceiver.EXTRA_DOSE_ID, dose.getId());
        intent.putExtra(MedicationReminderReceiver.EXTRA_DOSE_MED_NAME, medicine.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, dose.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {

            if (alarmManager.canScheduleExactAlarms()) {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger.getTimeInMillis(),
                        pendingIntent
                );

            } else {
                // We don't have permission yet
                // For now just log it so it doesn’t crash
                android.util.Log.e("AlarmScheduler", "Exact alarm permission not granted");
            }

            Log.d("AlarmScheduler", "canScheduleExactAlarms = " + alarmManager.canScheduleExactAlarms());
            Log.d("AlarmScheduler", "Scheduling alarm at: " + nextTrigger.getTime().toString());

        }
    }

    public static void cancelAlarm(Context context, int doseId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, MedicationReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, doseId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private static Calendar calculateNextTrigger(Medicine medicine, Dose dose) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        Date parsedTime;
        try {
            parsedTime = format.parse(dose.getTime());
        } catch (ParseException e){
            return null;
        }

        Calendar now = Calendar.getInstance();
        Calendar candidate = Calendar.getInstance();
        candidate.setTime(parsedTime);

        int hour = candidate.get(Calendar.HOUR_OF_DAY);
        int minute = candidate.get(Calendar.MINUTE);

        LocalDate today = LocalDate.now();

        for (int i = 0; i < 14; i++) {
            LocalDate checkDate = today.plusDays(i);

            if (!medicine.appliesOn(checkDate)) continue;

            Calendar cal = Calendar.getInstance();
            cal.set(checkDate.getYear(), checkDate.getMonthValue() - 1, checkDate.getDayOfMonth(), hour, minute, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (cal.after(now)) {
                return cal;
            }
        }
        return null;
    }

}

package com.example.medicationtracker.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {

    private static final String PREFS = "app_settings";
    private static final String KEY_REMINDERS_ENABLED = "reminders_enabled";
    private static final String KEY_SNOOZE_MINUTES = "snooze_minutes";

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean areRemindersEnabled() {
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true);
    }

    public void setRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply();
    }

    public int getSnoozeMinutes() {
        return prefs.getInt(KEY_SNOOZE_MINUTES, 10);
    }

    public void setSnoozeMinutes(int minutes) {
        prefs.edit().putInt(KEY_SNOOZE_MINUTES, minutes).apply();
    }
}

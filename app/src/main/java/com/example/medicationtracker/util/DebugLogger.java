package com.example.medicationtracker.util;

import android.content.Context;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DebugLogger {

    private static final String FILE_NAME = "alarm_debug_log.txt";

    public static void log(Context context, String message) {
        try {
            FileOutputStream fos =
                    context.openFileOutput(FILE_NAME, Context.MODE_APPEND);

            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            String timestamp = sdf.format(new Date());

            String fullMessage = timestamp + " | " + message + "\n";

            fos.write(fullMessage.getBytes());
            fos.close();

        } catch (Exception ignored) { }
    }
}

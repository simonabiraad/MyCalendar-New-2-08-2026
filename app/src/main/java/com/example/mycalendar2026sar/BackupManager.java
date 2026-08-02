package com.example.mycalendar2026sar;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.util.Map;

public class BackupManager {

    public static String createBackupJson(Context context) {
        try {
            JSONObject backup = new JSONObject();

            String[] prefsToBackup = {
                "CalendarNotes",
                "ArchivedNotes",
                "DeletedNotes",
                "SecureBoxNotes",
                "SecureBoxCategories",
                "AppColors",
                "AppFonts",
                "SecuritySettings",
                "ReminderStatus"
            };

            JSONObject allPrefs = new JSONObject();
            for (String prefsName : prefsToBackup) {
                SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
                JSONObject prefsJson = new JSONObject();
                Map<String, ?> allEntries = prefs.getAll();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    prefsJson.put(entry.getKey(), entry.getValue());
                }
                allPrefs.put(prefsName, prefsJson);
            }

            backup.put("preferences", allPrefs);
            backup.put("backup_time", System.currentTimeMillis());
            backup.put("app_version", "1.0");

            return backup.toString(4);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean restoreBackupJson(Context context, String jsonContent) {
        try {
            JSONObject backup = new JSONObject(jsonContent);
            if (!backup.has("preferences")) return false;

            JSONObject allPrefs = backup.getJSONObject("preferences");
            java.util.Iterator<String> keys = allPrefs.keys();

            while (keys.hasNext()) {
                String prefsName = keys.next();
                JSONObject prefsJson = allPrefs.getJSONObject(prefsName);
                SharedPreferences prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();

                java.util.Iterator<String> entryKeys = prefsJson.keys();
                while (entryKeys.hasNext()) {
                    String key = entryKeys.next();
                    Object value = prefsJson.get(key);

                    if (value instanceof Boolean) {
                        editor.putBoolean(key, (Boolean) value);
                    } else if (value instanceof Integer) {
                        editor.putInt(key, (Integer) value);
                    } else if (value instanceof Long) {
                        editor.putLong(key, (Long) value);
                    } else if (value instanceof Float) {
                        editor.putFloat(key, (Float) value);
                    } else if (value instanceof Double) {
                        // SharedPreferences doesn't support Double, store as float or string?
                        // Usually it's better to store as string if precision matters, but JSON parses decimals as Double.
                        editor.putFloat(key, ((Double) value).floatValue());
                    } else if (value instanceof String) {
                        editor.putString(key, (String) value);
                    }
                }
                editor.apply();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

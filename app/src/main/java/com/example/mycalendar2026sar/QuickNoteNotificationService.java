package com.example.mycalendar2026sar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class QuickNoteNotificationService extends Service {

    public static final String ACTION_VOICE = "com.example.mycalendar2026sar.ACTION_VOICE";
    public static final String ACTION_NOTE = "com.example.mycalendar2026sar.ACTION_NOTE";
    private static final String CHANNEL_ID = "quick_note_channel";
    private static final int NOTIF_ID = 1001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIF_ID, createNotification());
        return START_STICKY;
    }

    private Notification createNotification() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_widget);

        // Intent for voice
        Intent voiceIntent = new Intent(this, MainActivity.class);
        voiceIntent.setAction(ACTION_VOICE);
        voiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent voicePendingIntent = PendingIntent.getActivity(this, 0, voiceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.notif_voice_btn, voicePendingIntent);

        // Intent for note
        Intent noteIntent = new Intent(this, MainActivity.class);
        noteIntent.setAction(ACTION_NOTE);
        noteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent notePendingIntent = PendingIntent.getActivity(this, 1, noteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.notif_note_area, notePendingIntent);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_edit)
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Quick Note Bar",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Shows a persistent bar for quick note taking");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

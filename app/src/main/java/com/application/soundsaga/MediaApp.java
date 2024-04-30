package com.application.soundsaga;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

import com.application.soundsaga.feature.audiobook.MediaPlayerService;


public class MediaApp extends Application {
    public static final String NOTIFICATION_CHANNEL_ID = "MusicForegroundServiceNotification";
    public static final String NOTIFICATION_CHANNEL_NAME = "MusicForegroundService Notification Channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
        }
    }

}
package com.tgdownloader.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class DownloadService extends Service {
    private static final String CHANNEL = "downloads";

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel c = new NotificationChannel(
            CHANNEL, "Downloads",
            NotificationManager.IMPORTANCE_LOW
        );
        getSystemService(NotificationManager.class).createNotificationChannel(c);

        Notification n = new NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("TGDownloader")
            .setContentText("Downloads running")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build();

        startForeground(1001, n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

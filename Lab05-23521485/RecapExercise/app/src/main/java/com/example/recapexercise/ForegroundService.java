package com.example.recapexercise;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundChannel";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PROGRESS =
            "com.example.recapexercise.FOREGROUND_PROGRESS";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_MAX = "max";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = getSystemService(NotificationManager.class);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Foreground Service")
                        .setContentText("Running...")
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .build();

        startForeground(NOTIFICATION_ID, notification);

        new Thread(() -> {
            for (int i = 1; i <= 20; i++) {
                Log.d("ForegroundService", "Count = " + i);

                Intent broadcast = new Intent(ACTION_PROGRESS);
                broadcast.putExtra(EXTRA_PROGRESS, i);
                broadcast.putExtra(EXTRA_MAX, 20);
                sendBroadcast(broadcast);

                Notification updated = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Foreground Service")
                        .setContentText("Count: " + i + "/20")
                        .setSmallIcon(android.R.drawable.ic_media_play)
                        .build();
                notificationManager.notify(NOTIFICATION_ID, updated);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }

            Notification done = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Foreground Service")
                    .setContentText("Finished!")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .build();
            notificationManager.notify(NOTIFICATION_ID, done);

            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Foreground Channel",
                            NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

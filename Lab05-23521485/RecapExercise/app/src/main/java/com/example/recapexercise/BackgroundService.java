package com.example.recapexercise;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {

    public static final String ACTION_PROGRESS =
            "com.example.recapexercise.BACKGROUND_PROGRESS";
    public static final String EXTRA_PROGRESS = "progress";
    public static final String EXTRA_MAX = "max";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            for (int i = 1; i <= 20; i++) {
                Log.d("BackgroundService", "Count = " + i);

                Intent broadcast = new Intent(ACTION_PROGRESS);
                broadcast.putExtra(EXTRA_PROGRESS, i);
                broadcast.putExtra(EXTRA_MAX, 20);
                sendBroadcast(broadcast);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

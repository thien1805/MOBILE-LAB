package com.example.recapexercise;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class BoundCounterService extends Service {

    private final IBinder binder = new LocalBinder();
    private int count = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(() -> {
            while (true) {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    public int getCount() {
        return count;
    }

    public class LocalBinder extends Binder {
        BoundCounterService getService() {
            return BoundCounterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

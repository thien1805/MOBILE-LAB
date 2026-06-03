package com.example.fitnessapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.example.fitnessapp.MainActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.db.AppDatabase;
import com.example.fitnessapp.db.StepEntity;
import com.example.fitnessapp.model.StepData;
import com.example.fitnessapp.task.StepCalculationTask;

import java.util.Random;

public class StepLoggerService extends Service implements StepCalculationTask.CalculationCallback {

    private static final String CHANNEL_ID = "step_tracker_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long UPDATE_INTERVAL = 10000;

    private final IBinder binder = new StepBinder();
    private AppDatabase database;
    private Handler handler;
    private Runnable stepRunnable;
    private Random random;
    private StepData currentData;
    private boolean isRunning;
    private StepUpdateListener listener;

    public interface StepUpdateListener {
        void onStepDataUpdated(StepData data);
        void onStepLogged(int newSteps, double calories, int points);
    }

    public class StepBinder extends Binder {
        public StepLoggerService getService() {
            return StepLoggerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            database = AppDatabase.getInstance(this);
            handler = new Handler(Looper.getMainLooper());
            random = new Random();
            isRunning = false;
            createNotificationChannel();
            loadExistingData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExistingData() {
        new Thread(() -> {
            try {
                int totalSteps = database.stepDao().getTotalSteps();
                double totalCalories = database.stepDao().getTotalCalories();
                int totalPoints = database.stepDao().getTotalPoints();
                currentData = new StepData(totalSteps, totalCalories, totalPoints);
            } catch (Exception e) {
                e.printStackTrace();
                currentData = new StepData(0, 0, 0);
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            startForeground(NOTIFICATION_ID, createNotification(0, "Tracking your fitness journey..."));
        } catch (Exception e) {
            e.printStackTrace();
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!isRunning) {
            isRunning = true;
            startStepSimulation();
        }
        return START_STICKY;
    }

    private void startStepSimulation() {
        stepRunnable = new Runnable() {
            @Override
            public void run() {
                simulateStep();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
        handler.post(stepRunnable);
    }

    private void simulateStep() {
        int newSteps = random.nextInt(500) + 100;
        new StepCalculationTask(this).execute(newSteps);
    }

    @Override
    public void onCalculationComplete(StepData result) {
        new Thread(() -> {
            try {
                int newSteps = result.getTotalSteps();
                double calories = result.getTotalCalories();
                int points = result.getTotalPoints();

                StepEntity entity = new StepEntity(
                    newSteps,
                    System.currentTimeMillis(),
                    calories,
                    points
                );
                database.stepDao().insert(entity);

                int totalSteps = database.stepDao().getTotalSteps();
                double totalCalories = database.stepDao().getTotalCalories();
                int totalPoints = database.stepDao().getTotalPoints();

                currentData = new StepData(totalSteps, totalCalories, totalPoints);
                currentData.setMotivationalQuote(result.getMotivationalQuote());

                runOnUiThread(() -> {
                    updateNotification(totalSteps, currentData.getMotivationalQuote());
                    if (listener != null) {
                        listener.onStepLogged(newSteps, calories, points);
                        listener.onStepDataUpdated(currentData);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateNotification(int totalSteps, String quote) {
        try {
            Notification notification = createNotification(totalSteps, quote);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification(int steps, String quote) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String content = steps > 0
            ? steps + " steps today | " + quote
            : "Starting your fitness journey...";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fitness Tracker")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Step Tracker",
            NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("Shows your real-time step count and motivational quotes");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public StepData getCurrentData() {
        return currentData;
    }

    public void setListener(StepUpdateListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    private void runOnUiThread(Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (handler != null && stepRunnable != null) {
            handler.removeCallbacks(stepRunnable);
        }
    }
}

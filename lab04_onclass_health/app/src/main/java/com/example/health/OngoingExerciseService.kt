package com.example.health

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status

class OngoingExerciseService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                createNotificationChannel()
                val notification = buildNotification()
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Exercise")
            .setContentText("Ongoing Exercise")
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)

        val ongoingStatus = Status.Builder().addTemplate("Ongoing Exercise").build()
        val ongoingActivity = OngoingActivity.Builder(this, NOTIFICATION_ID, builder)
            .setStatus(ongoingStatus)
            .setTouchIntent(pendingIntent)
            .setStaticIcon(R.mipmap.ic_launcher)
            .build()

        ongoingActivity.apply(this)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Health Exercise",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows active workout status"
        }

        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.example.health.action.START_ONGOING"
        const val ACTION_STOP = "com.example.health.action.STOP_ONGOING"
        private const val CHANNEL_ID = "ongoing_exercise_channel"
        private const val NOTIFICATION_ID = 4001
    }
}


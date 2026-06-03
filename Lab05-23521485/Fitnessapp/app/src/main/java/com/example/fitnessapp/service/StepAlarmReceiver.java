package com.example.fitnessapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StepAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, StepLoggerService.class);
        context.startForegroundService(serviceIntent);
    }
}

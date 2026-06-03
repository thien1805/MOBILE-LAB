package com.example.recapexercise;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private BoundCounterService boundService;
    private boolean isBound = false;
    private TextView txtResult;
    private TextView txtServiceStatus;

    private final BroadcastReceiver backgroundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(BackgroundService.EXTRA_PROGRESS, 0);
            int max = intent.getIntExtra(BackgroundService.EXTRA_MAX, 20);
            txtServiceStatus.setText("Background: " + progress + "/" + max);
        }
    };

    private final BroadcastReceiver foregroundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(ForegroundService.EXTRA_PROGRESS, 0);
            int max = intent.getIntExtra(ForegroundService.EXTRA_MAX, 20);
            txtServiceStatus.setText("Foreground: " + progress + "/" + max);
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BoundCounterService.LocalBinder binder = (BoundCounterService.LocalBinder) service;
            boundService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtResult = findViewById(R.id.txtResult);
        txtServiceStatus = findViewById(R.id.txtServiceStatus);

        findViewById(R.id.btnBackground).setOnClickListener(v -> {
            txtServiceStatus.setText("Background: starting...");
            startService(new Intent(this, BackgroundService.class));
        });

        findViewById(R.id.btnForeground).setOnClickListener(v -> {
            txtServiceStatus.setText("Foreground: starting...");
            Intent intent = new Intent(this, ForegroundService.class);
            startForegroundService(intent);
        });

        findViewById(R.id.btnBind).setOnClickListener(v -> {
            Intent intent = new Intent(this, BoundCounterService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        });

        findViewById(R.id.btnGetCount).setOnClickListener(v -> {
            if (isBound && boundService != null) {
                txtResult.setText("Current count: " + boundService.getCount());
            } else {
                txtResult.setText("Service not bound");
            }
        });

        findViewById(R.id.btnAsyncTask).setOnClickListener(v ->
                new MyAsyncTask(txtResult).execute());

        findViewById(R.id.btnCoroutine).setOnClickListener(v ->
                CoroutineTask.execute(new CoroutineTask.Callback() {
                    @Override
                    public void onResult(String result) {
                        txtResult.setText(result);
                    }
                }));
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter bgFilter = new IntentFilter(BackgroundService.ACTION_PROGRESS);
        IntentFilter fgFilter = new IntentFilter(ForegroundService.ACTION_PROGRESS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(backgroundReceiver, bgFilter, Context.RECEIVER_NOT_EXPORTED);
            registerReceiver(foregroundReceiver, fgFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(backgroundReceiver, bgFilter);
            registerReceiver(foregroundReceiver, fgFilter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(backgroundReceiver);
        unregisterReceiver(foregroundReceiver);
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}

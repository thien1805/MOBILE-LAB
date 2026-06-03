package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private EditText edtLabel;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        timePicker = findViewById(R.id.timePicker);
        edtLabel = findViewById(R.id.edtLabel);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String label =
                    edtLabel.getText().toString();

            Toast.makeText(
                            this,
                            "Alarm Saved",
                            Toast.LENGTH_SHORT)
                    .show();

            finish();
        });
    }
}
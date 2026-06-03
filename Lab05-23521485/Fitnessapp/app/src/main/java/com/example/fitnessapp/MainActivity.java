package com.example.fitnessapp;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitnessapp.api.GeminiClient;
import com.example.fitnessapp.api.GeminiModels;
import com.example.fitnessapp.model.StepData;
import com.example.fitnessapp.service.StepBootReceiver;
import com.example.fitnessapp.service.StepLoggerService;

import java.text.NumberFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements StepLoggerService.StepUpdateListener {

    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private StepLoggerService stepService;
    private boolean isBound = false;

    private TextView valueSteps, valueCalories, valuePoints;
    private TextView valueLevel, progressText, nextMilestone;
    private TextView textQuote, textSuggestion, textEncouragement;
    private ProgressBar progressBar;
    private EditText inputApiKey;
    private Button btnGenerate;

    private View badgeBronzeContainer, badgeSilverContainer, badgeGoldContainer, badgePlatinumContainer;
    private CardView cardQuote;

    private int lastKnownSteps = 0;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepLoggerService.StepBinder binder = (StepLoggerService.StepBinder) service;
            stepService = binder.getService();
            stepService.setListener(MainActivity.this);
            isBound = true;
            if (stepService.getCurrentData() != null) {
                updateUI(stepService.getCurrentData());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stepService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupEdgeToEdge();
        requestNotificationPermission();
        initViews();
        startStepService();
        StepBootReceiver.scheduleAlarm(this);
        setupGeminiButton();
    }

    private void setupEdgeToEdge() {
        View root = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    private void initViews() {
        valueSteps = findViewById(R.id.valueSteps);
        valueCalories = findViewById(R.id.valueCalories);
        valuePoints = findViewById(R.id.valuePoints);
        valueLevel = findViewById(R.id.valueLevel);
        progressText = findViewById(R.id.progressText);
        nextMilestone = findViewById(R.id.nextMilestone);
        textQuote = findViewById(R.id.textQuote);
        textSuggestion = findViewById(R.id.textSuggestion);
        textEncouragement = findViewById(R.id.textEncouragement);
        progressBar = findViewById(R.id.progressBar);
        inputApiKey = findViewById(R.id.inputApiKey);
        btnGenerate = findViewById(R.id.btnGenerate);
        cardQuote = findViewById(R.id.cardQuote);

        badgeBronzeContainer = findViewById(R.id.badgeBronze);
        badgeSilverContainer = findViewById(R.id.badgeSilver);
        badgeGoldContainer = findViewById(R.id.badgeGold);
        badgePlatinumContainer = findViewById(R.id.badgePlatinum);

        badgeBronzeContainer.setAlpha(0.3f);
        badgeSilverContainer.setAlpha(0.3f);
        badgeGoldContainer.setAlpha(0.3f);
        badgePlatinumContainer.setAlpha(0.3f);
    }

    private void startStepService() {
        try {
            Intent intent = new Intent(this, StepLoggerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            Toast.makeText(this, "Service error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStepDataUpdated(StepData data) {
        runOnUiThread(() -> updateUI(data));
    }

    @Override
    public void onStepLogged(int newSteps, double calories, int points) {
        runOnUiThread(() -> {
            String msg = "+" + newSteps + " steps | +" + String.format("%.1f", calories) + " cal | +" + points + " pts";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUI(StepData data) {
        if (data == null) return;

        int totalSteps = data.getTotalSteps();
        double totalCalories = data.getTotalCalories();
        int totalPoints = data.getTotalPoints();

        valueSteps.setText(NumberFormat.getIntegerInstance().format(totalSteps));
        valueCalories.setText(String.format("%.1f", totalCalories));
        valuePoints.setText(NumberFormat.getIntegerInstance().format(totalPoints));

        int progress = Math.min(totalSteps, 10000);
        animateProgressBar(progress);

        String progressStr = NumberFormat.getIntegerInstance().format(totalSteps) + " / 10,000";
        progressText.setText(progressStr);

        int nextGoal = getNextMilestone(totalSteps);
        if (nextGoal > 0) {
            int remaining = nextGoal - totalSteps;
            nextMilestone.setText(remaining + " steps to " + NumberFormat.getIntegerInstance().format(nextGoal));
        } else {
            nextMilestone.setText("All milestones achieved!");
        }

        valueLevel.setText(getLevelTitle(totalSteps));

        if (data.getMotivationalQuote() != null) {
            textQuote.setText(data.getMotivationalQuote());
            animateQuoteCard();
        }

        checkBadges(totalSteps);
        lastKnownSteps = totalSteps;
    }

    private void animateProgressBar(int targetProgress) {
        ValueAnimator animator = ValueAnimator.ofInt(progressBar.getProgress(), targetProgress);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation ->
            progressBar.setProgress((int) animation.getAnimatedValue())
        );
        animator.start();
    }

    private void animateQuoteCard() {
        cardQuote.setScaleX(0.9f);
        cardQuote.setScaleY(0.9f);
        cardQuote.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new BounceInterpolator())
            .start();
    }

    private void animateBadgeUnlock(View badgeContainer) {
        badgeContainer.animate()
            .alpha(1f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction(() -> {
                badgeContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
            })
            .start();
    }

    private int getNextMilestone(int steps) {
        if (steps < 1000) return 1000;
        if (steps < 5000) return 5000;
        if (steps < 10000) return 10000;
        if (steps < 25000) return 25000;
        return -1;
    }

    private String getLevelTitle(int steps) {
        if (steps >= 25000) return "Platinum Champion";
        if (steps >= 10000) return "Gold Runner";
        if (steps >= 5000) return "Silver Strider";
        if (steps >= 1000) return "Bronze Walker";
        return "Novice Adventurer";
    }

    private void checkBadges(int totalSteps) {
        if (totalSteps >= 1000 && badgeBronzeContainer.getAlpha() < 1f) {
            animateBadgeUnlock(badgeBronzeContainer);
            Toast.makeText(this, "Badge Unlocked: Bronze Walker!", Toast.LENGTH_LONG).show();
        }
        if (totalSteps >= 5000 && badgeSilverContainer.getAlpha() < 1f) {
            animateBadgeUnlock(badgeSilverContainer);
            Toast.makeText(this, "Badge Unlocked: Silver Strider!", Toast.LENGTH_LONG).show();
        }
        if (totalSteps >= 10000 && badgeGoldContainer.getAlpha() < 1f) {
            animateBadgeUnlock(badgeGoldContainer);
            Toast.makeText(this, "Badge Unlocked: Gold Runner!", Toast.LENGTH_LONG).show();
        }
        if (totalSteps >= 25000 && badgePlatinumContainer.getAlpha() < 1f) {
            animateBadgeUnlock(badgePlatinumContainer);
            Toast.makeText(this, "Badge Unlocked: Platinum Champion!", Toast.LENGTH_LONG).show();
        }
    }

    private void setupGeminiButton() {
        btnGenerate.setOnClickListener(v -> {
            String apiKey = inputApiKey.getText().toString().trim();

            final int[] finalSteps = {lastKnownSteps};
            final double[] finalCalories = {0};
            final int[] finalPoints = {0};
            if (stepService != null && stepService.getCurrentData() != null) {
                StepData data = stepService.getCurrentData();
                finalSteps[0] = data.getTotalSteps();
                finalCalories[0] = data.getTotalCalories();
                finalPoints[0] = data.getTotalPoints();
            }
            int steps = finalSteps[0];
            double calories = finalCalories[0];
            int points = finalPoints[0];

            final int capturedSteps = steps;
            final double capturedCalories = calories;
            final int capturedPoints = points;

            if (apiKey.isEmpty()) {
                showOfflineSuggestion(steps, calories, points);
                return;
            }

            textSuggestion.setText(getString(R.string.gemini_generating));
            textEncouragement.setText("");
            btnGenerate.setEnabled(false);

            GeminiClient.getInstance().getWorkoutSuggestion(
                apiKey, capturedSteps, capturedCalories, capturedPoints,
                new Callback<GeminiModels.Response>() {
                    @Override
                    public void onResponse(Call<GeminiModels.Response> call,
                                           Response<GeminiModels.Response> response) {
                        btnGenerate.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().hasError()) {
                                textSuggestion.setText("API Error: " + response.body().getError().getMessage());
                                return;
                            }
                            if (response.body().getCandidates() != null
                                && !response.body().getCandidates().isEmpty()
                                && response.body().getCandidates().get(0).getContent() != null
                                && response.body().getCandidates().get(0).getContent().getParts() != null
                                && !response.body().getCandidates().get(0).getContent().getParts().isEmpty()) {

                                String result = response.body().getCandidates()
                                    .get(0).getContent().getParts().get(0).getText();
                                parseAndDisplayGeminiResult(result);
                            } else {
                                textSuggestion.setText("Empty response - no candidates returned");
                            }
                        } else if (response.code() == 429) {
                            showOfflineSuggestion(capturedSteps, capturedCalories, capturedPoints);
                            Toast.makeText(MainActivity.this,
                                "API quota exceeded. Using offline suggestion.",
                                Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMsg = "HTTP " + response.code();
                            try {
                                String errorBody = response.errorBody() != null
                                    ? response.errorBody().string() : "No error body";
                                errorMsg += ": " + errorBody;
                            } catch (Exception ignored) {}
                            textSuggestion.setText(errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiModels.Response> call, Throwable t) {
                        btnGenerate.setEnabled(true);
                        showOfflineSuggestion(capturedSteps, capturedCalories, capturedPoints);
                        Toast.makeText(MainActivity.this,
                            "Network error. Using offline suggestion.",
                            Toast.LENGTH_SHORT).show();
                    }
                }
            );
        });
    }

    private void showOfflineSuggestion(int steps, double calories, int points) {
        btnGenerate.setEnabled(true);

        String[][] workoutSuggestions = {
            {"Try a 15-minute brisk walk to keep the momentum going.",
             "Every step counts — you're building a healthier you!"},
            {"Do 10 minutes of stretching to improve flexibility.",
             "Your body will thank you for the care!"},
            {"Try 20 jumping jacks and 10 pushups for a quick burn.",
             "Consistency beats intensity — keep showing up!"},
            {"Take the stairs instead of the elevator today.",
             "Small changes lead to big results!"},
            {"Go for a 20-minute jog to boost your stamina.",
             "You're capable of more than you know!"},
            {"Try a 5-minute plank challenge to strengthen your core.",
             "Strength comes from the challenges you overcome!"},
            {"Stand up and walk around for 5 minutes every hour.",
             "Movement is medicine for body and mind!"},
            {"Do 15 squats and 10 lunges to build leg strength.",
             "Your legs are your foundation — make them strong!"},
            {"Try yoga for 10 minutes to improve balance and calm.",
             "A calm mind creates a strong body."},
            {"Dance to your favorite song for a fun cardio boost!",
             "Exercise doesn't have to be boring — enjoy the journey!"}
        };

        int idx = (int)(Math.random() * workoutSuggestions.length);
        String suggestion = workoutSuggestions[idx][0];
        String encouragement = workoutSuggestions[idx][1];

        if (steps > 5000) {
            encouragement = "You're crushing it with " + steps + " steps! " + encouragement;
        } else if (steps > 1000) {
            encouragement = "Great start with " + steps + " steps! " + encouragement;
        } else {
            encouragement = "Every journey begins with a single step. " + encouragement;
        }

        textSuggestion.setText(suggestion);
        textEncouragement.setText(encouragement);

        textSuggestion.setAlpha(0f);
        textSuggestion.animate().alpha(1f).setDuration(500).start();
        textEncouragement.setAlpha(0f);
        textEncouragement.animate().alpha(1f).setDuration(500).setStartDelay(200).start();
    }

    private void parseAndDisplayGeminiResult(String result) {
        String suggestion = "";
        String encouragement = "";

        if (result.contains("SUGGESTION:") && result.contains("ENCOURAGEMENT:")) {
            int sugIdx = result.indexOf("SUGGESTION:") + 11;
            int encIdx = result.indexOf("ENCOURAGEMENT:");
            suggestion = result.substring(sugIdx, encIdx).trim();
            encouragement = result.substring(encIdx + 14).trim();
        } else if (result.contains("SUGGESTION:")) {
            suggestion = result.substring(result.indexOf("SUGGESTION:") + 11).trim();
            if (suggestion.contains("ENCOURAGEMENT:")) {
                int idx = suggestion.indexOf("ENCOURAGEMENT:");
                encouragement = suggestion.substring(idx + 14).trim();
                suggestion = suggestion.substring(0, idx).trim();
            }
        } else if (result.contains(":")) {
            String[] parts = result.split("\n", 2);
            if (parts.length > 0) suggestion = parts[0].trim();
            if (parts.length > 1) encouragement = parts[1].trim();
        } else {
            suggestion = result;
        }

        if (!suggestion.isEmpty()) {
            textSuggestion.setText(suggestion);
        } else {
            textSuggestion.setText(result);
        }
        if (!encouragement.isEmpty()) {
            textEncouragement.setText(encouragement);
        }

        textSuggestion.setAlpha(0f);
        textSuggestion.animate().alpha(1f).setDuration(500).start();
        if (!textEncouragement.getText().toString().isEmpty()) {
            textEncouragement.setAlpha(0f);
            textEncouragement.animate().alpha(1f).setDuration(500).setStartDelay(200).start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isBound) {
            try {
                Intent intent = new Intent(this, StepLoggerService.class);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindServiceSafely();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindServiceSafely();
    }

    private void unbindServiceSafely() {
        if (isBound) {
            try {
                if (stepService != null) {
                    stepService.removeListener();
                }
                unbindService(connection);
            } catch (Exception ignored) {}
            isBound = false;
        }
    }
}

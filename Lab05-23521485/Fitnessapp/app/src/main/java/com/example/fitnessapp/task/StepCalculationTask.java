package com.example.fitnessapp.task;

import android.os.AsyncTask;
import com.example.fitnessapp.model.StepData;

public class StepCalculationTask extends AsyncTask<Integer, Void, StepData> {

    private final CalculationCallback callback;

    public interface CalculationCallback {
        void onCalculationComplete(StepData result);
    }

    public StepCalculationTask(CalculationCallback callback) {
        this.callback = callback;
    }

    @Override
    protected StepData doInBackground(Integer... params) {
        int steps = params[0];
        double calories = steps * 0.04;
        int points = steps / 100;
        StepData data = new StepData(steps, calories, points);
        String[] quotes = {
            "The only bad workout is the one that didn't happen.",
            "Your body can stand almost anything. It's your mind you have to convince.",
            "The struggle you're in today is developing the strength you need for tomorrow.",
            "Believe in yourself and all that you are.",
            "You are stronger than you think.",
            "Push yourself because no one else is going to do it for you.",
            "Success starts with self-discipline.",
            "Don't wish for it, work for it!"
        };
        data.setMotivationalQuote(quotes[(int)(Math.random() * quotes.length)]);
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        return data;
    }

    @Override
    protected void onPostExecute(StepData result) {
        if (callback != null) {
            callback.onCalculationComplete(result);
        }
    }
}

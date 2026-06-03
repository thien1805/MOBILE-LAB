package com.example.simpleasynctask;

import android.os.AsyncTask;
import android.widget.TextView;

import java.util.Random;

public class SimpleAsyncTask extends AsyncTask<Void, Void, String> {

    private TextView mTextView;

    public SimpleAsyncTask(TextView tv) {
        mTextView = tv;
    }

    @Override
    protected String doInBackground(Void... voids) {

        Random random = new Random();

        int n = random.nextInt(11);

        int sleepTime = n * 200;

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "Awake at last after sleeping for "
                + sleepTime
                + " milliseconds!";
    }

    @Override
    protected void onPostExecute(String result) {
        mTextView.setText(result);
    }
}
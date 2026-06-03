package com.example.recapexercise;

import android.os.AsyncTask;
import android.widget.TextView;

public class MyAsyncTask extends AsyncTask<Void, Void, String> {

    private TextView textView;

    public MyAsyncTask(TextView textView) {
        this.textView = textView;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
        return "AsyncTask Finished";
    }

    @Override
    protected void onPostExecute(String result) {
        textView.setText(result);
    }
}

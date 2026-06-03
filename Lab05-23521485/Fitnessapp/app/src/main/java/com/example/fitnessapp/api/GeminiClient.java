package com.example.fitnessapp.api;

import android.util.Log;

import com.example.fitnessapp.api.GeminiModels.*;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static final String TAG = "GeminiClient";
    private static GeminiClient instance;
    private final GeminiApi api;
    private final Gson gson;

    private GeminiClient() {
        this.gson = new Gson();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
            Log.d(TAG, message)
        );
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        api = retrofit.create(GeminiApi.class);
    }

    public static synchronized GeminiClient getInstance() {
        if (instance == null) {
            instance = new GeminiClient();
        }
        return instance;
    }

    public void getWorkoutSuggestion(String apiKey, int steps, double calories, int points,
                                      Callback<Response> callback) {
        String prompt = "You are a fitness coach. Based on this data: " + steps + " steps, " +
            String.format("%.1f", calories) + " cal, " + points + " pts. " +
            "Give me:\n1) A SHORT workout suggestion (one sentence)\n2) A SHORT encouragement (one sentence)\n" +
            "Format exactly like this with labels:\nSUGGESTION: ...\nENCOURAGEMENT: ...";

        Part part = new Part(prompt);
        Content content = new Content(Collections.singletonList(part));
        Request request = new Request(Collections.singletonList(content));

        Log.d(TAG, "Request: " + gson.toJson(request));

        Call<Response> call = api.generateContent(apiKey, request);
        call.enqueue(callback);
    }
}

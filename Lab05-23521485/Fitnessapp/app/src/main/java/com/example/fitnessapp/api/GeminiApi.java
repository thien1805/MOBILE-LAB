package com.example.fitnessapp.api;

import com.example.fitnessapp.api.GeminiModels.Request;
import com.example.fitnessapp.api.GeminiModels.Response;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApi {
    @POST("v1/models/gemini-2.0-flash:generateContent")
    Call<Response> generateContent(
        @Query("key") String apiKey,
        @Body Request request
    );
}

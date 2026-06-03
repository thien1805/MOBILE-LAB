package com.example.homework.api

import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayService {

    @GET("api/")
    suspend fun getImages(
        @Query("key") key: String,
        @Query("q") query: String
    ): PixabayResponse
}
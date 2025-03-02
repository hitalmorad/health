package com.example.health.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

// Define the data model (adjust based on API response)


// Retrofit API Service
interface ApifyApiService {
    @GET
    suspend fun getDoctors(@Url url: String): List<Doctor>
}

// Retrofit Instance
object RetrofitClient {
    val apiService: ApifyApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.apify.com/v2/") // Base URL (won't be used with @Url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApifyApiService::class.java)
    }
}

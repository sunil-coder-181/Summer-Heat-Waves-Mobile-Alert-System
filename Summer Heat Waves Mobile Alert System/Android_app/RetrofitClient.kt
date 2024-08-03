package com.example.android

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Default base URL
    private const val BASE_URL = "https://flaskapp-1-2h2z.onrender.com/" // Note the trailing slash

    // Retrofit instance with the default BASE_URL
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Method to get the Retrofit client with the default BASE_URL
    fun getDefaultClient(): Retrofit {
        return retrofit
    }

    // Method to get the Retrofit client with a custom base URL
    fun getClient(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}

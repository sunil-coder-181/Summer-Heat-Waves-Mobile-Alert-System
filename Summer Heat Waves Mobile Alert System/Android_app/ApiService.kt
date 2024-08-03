package com.example.android

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/predict-live-heat-wave")
    fun predictHeatWave(@Body request: PredictionRequest): Call<PredictionResponse>
}

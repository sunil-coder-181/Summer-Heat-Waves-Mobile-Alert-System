package com.example.android

data class PredictionRequest(
    val api_key: String,
    val token: String,
    val latitude: Double,
    val longitude: Double
)


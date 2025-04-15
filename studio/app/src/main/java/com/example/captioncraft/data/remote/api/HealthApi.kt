package com.example.captioncraft.data.remote.api

import com.example.captioncraft.data.remote.dto.HealthResponse
import retrofit2.http.GET

interface HealthApi {
    @GET("/")
    suspend fun checkHealth(): HealthResponse
} 
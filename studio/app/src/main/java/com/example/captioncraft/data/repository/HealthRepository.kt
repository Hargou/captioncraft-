package com.example.captioncraft.data.repository

import com.example.captioncraft.data.remote.api.HealthApi
import com.example.captioncraft.data.remote.dto.HealthResponse
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class HealthRepository @Inject constructor(
    private val api: HealthApi
) {
    suspend fun checkHealth(): HealthResponse {
        return api.checkHealth()
    }
} 
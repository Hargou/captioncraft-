package com.example.captioncraft.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.captioncraft.data.repository.HealthRepository
import com.example.captioncraft.data.remote.dto.HealthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val repository: HealthRepository
) : ViewModel() {

    private val _healthState = MutableStateFlow<HealthState>(HealthState.Loading)
    val healthState: StateFlow<HealthState> = _healthState

    fun checkHealth() {
        viewModelScope.launch {
            try {
                val response = repository.checkHealth()
                _healthState.value = HealthState.Success(response)
            } catch (e: Exception) {
                _healthState.value = HealthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class HealthState {
    object Loading : HealthState()
    data class Success(val response: HealthResponse) : HealthState()
    data class Error(val message: String) : HealthState()
} 
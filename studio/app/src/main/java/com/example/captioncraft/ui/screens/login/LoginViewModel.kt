package com.example.captioncraft.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.captioncraft.data.repository.LocalRepository
import com.example.captioncraft.data.repository.UserRepository
import com.example.captioncraft.domain.model.User
import com.example.captioncraft.data.remote.dto.RegisterDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var name by mutableStateOf("")
        private set

    private val _loginStatus = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginStatus: StateFlow<LoginUiState> = _loginStatus.asStateFlow()

    private val _registerStatus = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val registerStatus: StateFlow<RegisterUiState> = _registerStatus.asStateFlow()

    fun onUsernameChanged(newValue: String) {
        username = newValue
    }

    fun onPasswordChanged(newValue: String) {
        password = newValue
    }

    fun onNameChanged(newValue: String) {
        name = newValue
    }

    fun login() {
        viewModelScope.launch {
            _loginStatus.value = LoginUiState.Loading
            try {
                val result = userRepository.login(username, password)
                result.onSuccess { user ->
                    localRepository.login(username, password)
                    _loginStatus.value = LoginUiState.Success
                }.onFailure { error ->
                    _loginStatus.value = LoginUiState.Error(error.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginStatus.value = LoginUiState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _registerStatus.value = RegisterUiState.Loading
            try {
                val result = userRepository.register(username, name, password)
                if (result.isSuccess) {
                    login()
                } else {
                    _registerStatus.value = RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _registerStatus.value = RegisterUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearRegisterResult() {
        _registerStatus.value = RegisterUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
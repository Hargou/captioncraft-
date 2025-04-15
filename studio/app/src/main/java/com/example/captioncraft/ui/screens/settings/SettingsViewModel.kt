package com.example.captioncraft.ui.screens.settings
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(true)
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    private val _isPrivateModeEnabled = MutableStateFlow(false)
    val isPrivateModeEnabled: StateFlow<Boolean> = _isPrivateModeEnabled.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun toggleNotifications(enabled: Boolean) {
        _isNotificationsEnabled.value = enabled
    }

    fun togglePrivateMode(enabled: Boolean) {
        _isPrivateModeEnabled.value = enabled
    }
}
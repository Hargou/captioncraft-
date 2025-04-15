package com.example.captioncraft.data.remote.dto

data class SettingsDto(
    val id: Int,
    val darkModeEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val privateModeEnabled: Boolean
)
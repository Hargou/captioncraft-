package com.example.captioncraft.domain.mapper

import com.example.captioncraft.data.local.entity.SettingsEntity
import com.example.captioncraft.data.remote.dto.SettingsDto
import com.example.captioncraft.domain.model.UserSettings

fun SettingsDto.toDomain(): UserSettings = UserSettings(
    id = id,
    darkModeEnabled = darkModeEnabled,
    notificationsEnabled = notificationsEnabled,
    privateModeEnabled = privateModeEnabled
)

fun UserSettings.toDto(): SettingsDto = SettingsDto(
    id = id,
    darkModeEnabled = darkModeEnabled,
    notificationsEnabled = notificationsEnabled,
    privateModeEnabled = privateModeEnabled
)

fun SettingsEntity.toDomain(): UserSettings = UserSettings(
    id = id,
    darkModeEnabled = darkModeEnabled,
    notificationsEnabled = notificationsEnabled,
    privateModeEnabled = privateModeEnabled
)

fun UserSettings.toEntity(): SettingsEntity = SettingsEntity(
    id = id,
    darkModeEnabled = darkModeEnabled,
    notificationsEnabled = notificationsEnabled,
    privateModeEnabled = privateModeEnabled
)

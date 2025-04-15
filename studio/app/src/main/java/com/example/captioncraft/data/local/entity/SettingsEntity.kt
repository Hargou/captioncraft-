package com.example.captioncraft.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.*

@Entity(
    tableName = "settings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("id")]
)
data class SettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val darkModeEnabled: Boolean,
    val notificationsEnabled: Boolean,
    val privateModeEnabled: Boolean
)

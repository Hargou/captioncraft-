package com.example.captioncraft.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val username: String,
    val password: String,
    val name: String,
    val profilePicture: String? = null,
    val createdAt: Date? = Date(),
)


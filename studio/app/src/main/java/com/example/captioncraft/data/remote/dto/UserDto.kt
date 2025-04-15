package com.example.captioncraft.data.remote.dto

data class UserDto(
    val id: Int,
    val username: String,
    val name: String,
    val password: String,
    val profilePicture: String?,
    val created_at: String
)


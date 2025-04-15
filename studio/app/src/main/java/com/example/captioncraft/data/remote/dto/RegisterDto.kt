package com.example.captioncraft.data.remote.dto

data class RegisterDto(
    val username: String,
    val name: String,
    val password: String,
    val profilePicture: String? = null
)

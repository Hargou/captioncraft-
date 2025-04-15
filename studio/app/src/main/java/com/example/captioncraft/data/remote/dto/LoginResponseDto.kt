package com.example.captioncraft.data.remote.dto

data class LoginResponseDto(
    val status: String,
    val message: String,
    val data: UserDto?
) 
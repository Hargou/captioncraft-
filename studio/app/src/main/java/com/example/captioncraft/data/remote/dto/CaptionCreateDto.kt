package com.example.captioncraft.data.remote.dto

data class CaptionCreateDto (
    val postId: Int,
    val userId: Int,
    val password: String,
    val text: String
)
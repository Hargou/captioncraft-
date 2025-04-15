package com.example.captioncraft.data.remote.dto

data class CaptionDto(
    val id: Int,
    val postId: Int,
    val userId: Int,
    val text: String,
    val created_at: String,
    val likes: Int,
    val username: String? = null
)
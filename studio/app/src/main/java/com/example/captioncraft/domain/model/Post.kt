package com.example.captioncraft.domain.model

data class Post(
    val id: Int,
    val userId: Int,
    val imageUrl: String,
    val createdAt: String,
    val likeCount: Int,
    val captionCount: Int,
    val captions: List<Caption> = emptyList()
)

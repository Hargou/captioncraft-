package com.example.captioncraft.domain.model

data class Post(
    val id: Int,
    val userId: Int,
    val imageUrl: String,
    val createdAt: String,
    val likeCount: Int = 0,
    val captionCount: Int = 0,
    val captions: List<Caption> = emptyList(),
    val username: String = "",
    val content: String = "",
    val likes: Int = likeCount,
    val likedByUser: Boolean = false
)

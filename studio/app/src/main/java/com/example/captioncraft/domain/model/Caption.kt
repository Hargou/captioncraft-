package com.example.captioncraft.domain.model

import java.util.Date

data class Caption(
    val id: Int,
    val postId: Int,
    val userId: Int,
    val text: String,
    val createdAt: Date?,
    val likes: Int
)

package com.example.captioncraft.domain.model

import java.util.Date

data class Comment(
    val id: Int,
    val captionId: Int,
    val userId: Int,
    val username: String,
    val text: String,
    val createdAt: Date
) 
package com.example.captioncraft.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val imageUrl: String,
    val createdAt: String,
    val likeCount: Int,
    val captionCount: Int,
    val username: String = ""
)


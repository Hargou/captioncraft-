package com.example.captioncraft.data.local.entity

import androidx.room.*
import java.util.Date

@Entity(
    tableName = "Caption",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId"), Index("userId")]
)
data class CaptionEntity(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val postId: Int,
    val userId: Int,
    val text: String,
    val createdAt: Date = Date(),
    val likes: Int = 0
)


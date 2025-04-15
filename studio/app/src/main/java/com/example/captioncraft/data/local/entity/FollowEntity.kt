package com.example.captioncraft.data.local.entity

import androidx.room.*
import java.util.Date

@Entity(
    tableName = "Follow",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["followingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = ["followerId", "followingId"],
    indices = [Index("followerId"), Index("followingId")]
)
data class FollowEntity(
    val followerId: Int,
    val followingId: Int,
    val createdAt: Date = Date()
)


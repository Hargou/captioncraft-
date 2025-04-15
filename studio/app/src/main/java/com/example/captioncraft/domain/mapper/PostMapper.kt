package com.example.captioncraft.domain.mapper

import com.example.captioncraft.data.local.entity.PostEntity
import com.example.captioncraft.data.remote.dto.PostDto
import com.example.captioncraft.domain.model.Post
import java.util.Date

fun PostDto.toDomain(): Post = Post(
    id = id,
    userId = userId,
    imageUrl = imageUrl,
    createdAt = createdAt,
    likeCount = likeCount,
    captionCount = captionCount
)

fun PostEntity.toDomain(): Post = Post(
    id = id,
    userId = userId,
    imageUrl = imageUrl,
    createdAt = createdAt,
    likeCount = likeCount,
    captionCount = captionCount
)

fun Post.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    imageUrl = imageUrl,
    createdAt = createdAt,
    likeCount = likeCount,
    captionCount = captionCount
)

fun PostDto.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    imageUrl = imageUrl,
    createdAt = createdAt,
    likeCount = likeCount,
    captionCount = captionCount
)

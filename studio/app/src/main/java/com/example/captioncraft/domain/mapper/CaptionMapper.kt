package com.example.captioncraft.domain.mapper

import com.example.captioncraft.data.local.entity.CaptionEntity
import com.example.captioncraft.data.remote.dto.CaptionCreateDto
import com.example.captioncraft.data.remote.dto.CaptionDto
import com.example.captioncraft.domain.model.Caption
import java.util.Date

fun CaptionDto.toDomain() = Caption(
    id = id, 
    postId = postId, 
    userId = userId, 
    text = text, 
    createdAt = parseIsoDateNonNull(created_at), 
    likes = likes,
    username = username ?: "User $userId"
)


fun Caption.toEntity() = CaptionEntity(
    id = id, 
    postId = postId, 
    userId = userId, 
    text = text, 
    createdAt = createdAt ?: Date(), 
    likes = likes,
    username = username
)

fun CaptionEntity.toDomain() = Caption(
    id = id, 
    postId = postId, 
    userId = userId, 
    text = text, 
    createdAt = createdAt, 
    likes = likes,
    username = username ?: "User $userId"
)

fun Caption.toRequestDto(password: String) = CaptionCreateDto(
    postId = postId,
    userId = userId,
    password = password,
    text = text
)
package com.example.captioncraft.domain.mapper

import com.example.captioncraft.data.local.entity.CaptionEntity
import com.example.captioncraft.data.remote.dto.CaptionCreateDto
import com.example.captioncraft.data.remote.dto.CaptionDto
import com.example.captioncraft.domain.model.Caption
import java.util.Date

fun CaptionDto.toDomain() = Caption(
    id, postId, userId, text, parseIsoDateNonNull(created_at), likes
)


fun Caption.toEntity() = CaptionEntity(
    id = id, 
    postId = postId, 
    userId = userId, 
    text = text, 
    createdAt = createdAt ?: Date(), 
    likes = likes
)

fun CaptionEntity.toDomain() = Caption(id, postId, userId, text, createdAt, likes)

fun Caption.toRequestDto(password: String) = CaptionCreateDto(
    postId = postId,
    userId = userId,
    password = password,
    text = text
)
package com.example.captioncraft.domain.mapper

import com.example.captioncraft.data.remote.dto.CommentDto
import com.example.captioncraft.domain.model.Comment

fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        captionId = captionId,
        userId = userId,
        username = username,
        text = text,
        createdAt = getCreatedAtDate()
    )
} 
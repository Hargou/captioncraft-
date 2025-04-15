package com.example.captioncraft.domain.mapper

import com.example.captioncraft.data.local.entity.UserEntity
import com.example.captioncraft.data.remote.dto.UserDto
import com.example.captioncraft.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    name = name,
    profilePicture = profilePicture,
    createdAt = createdAt
)

fun UserDto.toDomain(): User = User(
    id = id,
    username = username,
    name = name,
    profilePicture = profilePicture,
    createdAt = parseIsoDate(created_at)
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    username = username,
    name = name,
    password = "", // This should be handled by the authentication system
    profilePicture = profilePicture,
    createdAt = createdAt
)


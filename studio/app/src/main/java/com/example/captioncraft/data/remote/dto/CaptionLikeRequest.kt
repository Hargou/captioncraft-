package com.example.captioncraft.data.remote.dto

data class CaptionLikeRequest(
    val captionId: Int,
    val userId: Int,
    val password: String
) 
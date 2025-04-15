package com.example.captioncraft.data.remote.dto

data class CaptionCreateResponse(
    val status: String,
    val message: String,
    val data: CaptionIdData
)

data class CaptionIdData(
    val captionId: Int
) 
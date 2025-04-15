package com.example.captioncraft.data.remote.dto

data class CaptionResponse(
    val status: String,
    val message: String,
    val data: List<List<Any>>
) {
    companion object {
        fun toCaptionDtoList(response: CaptionResponse): List<CaptionDto> {
            return response.data.map { captionData ->
                CaptionDto(
                    id = (captionData[0] as? Double)?.toInt() ?: -1,
                    postId = (captionData[1] as? Double)?.toInt() ?: -1,
                    userId = (captionData[2] as? Double)?.toInt() ?: -1,
                    text = captionData[3] as String,
                    created_at = captionData[4] as String,
                    likes = (captionData[5] as? Double)?.toInt() ?: 0
                )
            }
        }
    }
} 
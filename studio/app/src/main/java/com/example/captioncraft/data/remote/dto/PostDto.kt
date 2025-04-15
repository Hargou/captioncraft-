package com.example.captioncraft.data.remote.dto

import com.google.gson.annotations.SerializedName
import android.util.Log

data class PostResponse(
    val status: String,
    val message: String,
    val data: List<List<Any>>
)

data class PostDto(
    val id: Int,
    val userId: Int,
    val imageUrl: String,
    val createdAt: String,
    val likeCount: Int,
    val captionCount: Int
) {
    companion object {
        fun fromArray(array: List<Any>): PostDto {
            val imageName = array[2] as String
            
            // Debug the array content
            Log.d("PostDto", "Array content: $array")
            
            // Caption count is at index 6 (7th position)
            val captionCount = if (array.size > 6 && array[6] != null) {
                try {
                    (array[6] as? Double)?.toInt() ?: 0
                } catch (e: Exception) {
                    Log.e("PostDto", "Error parsing caption count from $array", e)
                    0
                }
            } else {
                0
            }
            
            Log.d("PostDto", "Parsed caption count: $captionCount for post ID: ${(array[0] as? Double)?.toInt() ?: -1}")
            
            return PostDto(
                id = (array[0] as? Double)?.toInt() ?: -1,
                userId = (array[1] as? Double)?.toInt() ?: -1,
                imageUrl = "http://10.0.2.2:8000/user_post_images/$imageName",
                createdAt = array[3] as String,
                likeCount = (array[4] as? Double)?.toInt() ?: 0,
                captionCount = captionCount
            )
        }
    }
}
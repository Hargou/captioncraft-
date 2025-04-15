package com.example.captioncraft.data.remote.dto

import com.google.gson.annotations.SerializedName
import android.util.Log

data class PostResponse(
    val status: String,
    val message: String,
    val data: List<List<Any>>
)

// New response class for post creation
data class PostCreateResponse(
    val status: String,
    val message: String,
    val data: PostCreateData
)

data class PostCreateData(
    val postId: Int,
    val imageName: String
)

data class PostDto(
    val id: Int,
    val userId: Int,
    val imageUrl: String,
    val createdAt: String,
    val likeCount: Int,
    val captionCount: Int,
    val username: String = ""
) {
    companion object {
        private const val BASE_URL = "http://10.0.2.2:8000"
        
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
            
            // Username is at index 7 (8th position)
            val username = if (array.size > 7 && array[7] != null) {
                try {
                    array[7] as String
                } catch (e: Exception) {
                    Log.e("PostDto", "Error parsing username from $array", e)
                    ""
                }
            } else {
                ""
            }
            
            Log.d("PostDto", "Parsed caption count: $captionCount, username: $username for post ID: ${(array[0] as? Double)?.toInt() ?: -1}")
            
            return PostDto(
                id = (array[0] as? Double)?.toInt() ?: -1,
                userId = (array[1] as? Double)?.toInt() ?: -1,
                imageUrl = "$BASE_URL/post/user_post_images/$imageName",
                createdAt = array[3] as String,
                likeCount = (array[4] as? Double)?.toInt() ?: 0,
                captionCount = captionCount,
                username = username
            )
        }
    }
}
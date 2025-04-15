package com.example.captioncraft.data.repository

import android.util.Log
import com.example.captioncraft.data.local.dao.CaptionDao
import com.example.captioncraft.data.remote.api.CaptionApi
import com.example.captioncraft.data.remote.dto.CaptionCreateDto
import com.example.captioncraft.data.remote.dto.CaptionDto
import com.example.captioncraft.data.remote.dto.CaptionLikeRequest
import com.example.captioncraft.data.remote.dto.CaptionResponse
import com.example.captioncraft.data.remote.dto.CommentCreateDto
import com.example.captioncraft.domain.mapper.toDomain
import com.example.captioncraft.domain.model.Caption
import com.example.captioncraft.domain.model.Comment
import com.example.captioncraft.util.UserSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import java.util.Date
import javax.inject.Inject

private const val TAG = "CaptionRepository"

class CaptionRepository @Inject constructor(
    private val captionDao: CaptionDao,
    private val captionApi: CaptionApi,
    private val sessionManager: UserSessionManager
) {
    fun getCaptionsForPost(postId: Int): Flow<List<Caption>> {
        return flow {
            try {
                val response = captionApi.getCaptions(postId)
                Log.d(TAG, "Got captions response for post $postId: ${response.data.size} items")
                
                // Use the helper method to convert response to DTOs
                val captionsDto = CaptionResponse.toCaptionDtoList(response)
                
                // Map to domain objects
                val captions = captionsDto.mapIndexed { index, dto ->
                    try {
                        val caption = dto.toDomain()
                        Log.d(TAG, "Mapped CaptionDto to domain $index: id=${caption.id}, text='${caption.text}', username=${caption.username}")
                        caption
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping CaptionDto to domain at index $index", e)
                        null
                    }
                }.filterNotNull()
                
                Log.d(TAG, "Processed ${captions.size} captions for post $postId")
                emit(captions)
            } catch (e: Exception) {
                // Handle exceptions properly
                Log.e(TAG, "Error fetching captions for post $postId", e)
                // Emit empty list for all exceptions
                emit(emptyList())
            }
        }.catch { e ->
            // Handle exceptions properly with the Flow.catch operator
            Log.e(TAG, "Error in caption flow for post $postId", e)
            // Emit empty list for all exceptions
            emit(emptyList())
        }
    }

    suspend fun addCaption(postId: Int, userId: Int, text: String) : Result<Int> {
        return try {
            val password = sessionManager.getUserPassword()
            if (password.isEmpty()) {
                return Result.failure(IllegalStateException("No password available. Please log in again."))
            }
            
            val caption = CaptionCreateDto(
                postId = postId,
                userId = userId,
                password = password,
                text = text
            )
            val response = captionApi.createCaption(caption)
            
            // We only get the ID back, not the full caption
            val captionId = response.data.captionId
            Log.d(TAG, "Successfully created caption with ID: $captionId")
            Result.success(captionId)
        } catch (e : Exception) {
            Log.e(TAG, "Error adding caption", e)
            Result.failure(e)
        }
    }

    // Updated to handle both checking like status and toggling like
    suspend fun toggleLike(captionId: Int, userId: Int, isCurrentlyLiked: Boolean) {
        try {
            val password = sessionManager.getUserPassword()
            if (password.isEmpty()) {
                throw IllegalStateException("No password available. Please log in again.")
            }
            
            val likeRequest = CaptionLikeRequest(
                captionId = captionId,
                userId = userId,
                password = password
            )
            
            captionApi.likeCaption(likeRequest)
            Log.d(TAG, "Successfully toggled like for caption $captionId")
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like for caption $captionId", e)
            throw e
        }
    }
    
    // Comment methods
    suspend fun getComments(captionId: Int): Result<List<Comment>> {
        return try {
            val comments = captionApi.getComments(captionId).map { it.toDomain() }
            Result.success(comments)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching comments for caption $captionId", e)
            Result.failure(e)
        }
    }
    
    suspend fun addComment(captionId: Int, userId: Int, text: String): Result<Comment> {
        return try {
            val password = sessionManager.getUserPassword()
            if (password.isEmpty()) {
                return Result.failure(IllegalStateException("No password available. Please log in again."))
            }
            
            val comment = CommentCreateDto(
                captionId = captionId,
                userId = userId,
                password = password,
                text = text
            )
            
            val response = captionApi.addComment(comment)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment to caption $captionId", e)
            Result.failure(e)
        }
    }
}

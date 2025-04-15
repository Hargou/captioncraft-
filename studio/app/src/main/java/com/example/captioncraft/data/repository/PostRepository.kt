package com.example.captioncraft.data.repository

import android.util.Log
import com.example.captioncraft.data.local.dao.FollowDao
import com.example.captioncraft.data.local.dao.PostDao
import com.example.captioncraft.data.remote.api.PostApi
import com.example.captioncraft.data.remote.dto.PostDto
import com.example.captioncraft.data.remote.dto.LikeRequest
import com.example.captioncraft.domain.mapper.toDomain
import com.example.captioncraft.domain.mapper.toEntity
import com.example.captioncraft.domain.model.Post
import com.example.captioncraft.util.UserSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.catch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val followDao: FollowDao,
    private val postApi: PostApi,
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) {
    suspend fun clearCache() {
        postDao.deleteAllPosts()
    }

    fun getFeedPosts(): Flow<List<Post>> = flow {
        // First, try to get posts from local database
        val currentUser = userRepository.currentUser.first() ?: throw IllegalStateException("No user logged in")
        
        // Try to fetch from server first
        try {
            val posts = postApi.getAllPosts()
            // Delete all existing posts before inserting new ones
            postDao.deleteAllPosts()
            val postEntities = posts.data.map { PostDto.fromArray(it).toEntity() }
            postDao.insertPosts(postEntities)
        } catch (e: Exception) {
            Log.e("PostRepository", "Error fetching posts from server", e)
        }

        // Then emit posts from local database
        postDao.getAllPosts().collect { posts ->
            emit(posts.map { it.toDomain() })
        }
    }.catch { e ->
        Log.e("PostRepository", "Error in getFeedPosts", e)
        // Emit empty list for all exceptions
        emit(emptyList())
    }

    suspend fun addCaption(postId: Int, userId: Int, text: String) {
        // This functionality is handled by the CaptionRepository
    }

    suspend fun toggleCaptionLike(postId: Int, captionId: Int) {
        // This functionality is handled by the CaptionRepository
    }

    suspend fun getPostById(id: Int): Post {
        val response = postApi.getPostById(id)
        return PostDto.fromArray(response.data[0]).toDomain()
    }

    suspend fun getAllPosts(): List<Post> {
        val response = postApi.getAllPosts()
        return response.data.map { PostDto.fromArray(it).toDomain() }
    }

    suspend fun getPostsByUser(userId: Int): List<Post> {
        val response = postApi.getPostsByUser(userId)
        return response.data.map { PostDto.fromArray(it).toDomain() }
    }

    suspend fun createPost(userId: Int, password: String, imageFile: File, caption: String? = null): Post {
        val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
        val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
        val captionBody = caption?.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = postApi.createPost(userIdBody, passwordBody, imagePart, captionBody)
        return PostDto.fromArray(response.data[0]).toDomain()
    }

    suspend fun updatePost(post: Post) {
        // This functionality is not implemented in the backend yet
    }

    suspend fun deletePost(id: Int) {
        postApi.deletePost(id)
    }

    suspend fun toggleLike(postId: Int) {
        try {
            // Get the current user ID
            val currentUser = userRepository.currentUser.first() ?: throw IllegalStateException("No user logged in")
            
            // Get password from session manager
            val password = sessionManager.getUserPassword()
            if (password.isEmpty()) {
                throw IllegalStateException("No password available. Please log in again.")
            }
            
            // Call the server API to like the post
            try {
                val likeRequest = LikeRequest(
                    postId = postId, 
                    userId = currentUser.id,
                    password = password
                )
                val response = postApi.likePost(likeRequest)
                Log.d("PostRepository", "Post liked on server: $postId")
                
                // Update local database directly
                updateLocalPostLikeCount(postId)
            } catch (e: Exception) {
                // If server call fails, update the local database as a fallback
                Log.e("PostRepository", "Error liking post on server, falling back to local update", e)
                updateLocalPostLikeCount(postId)
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Error toggling like for post $postId", e)
            throw e
        }
    }
    
    // Helper method to update the local post like count
    private suspend fun updateLocalPostLikeCount(postId: Int) {
        val posts = postDao.getAllPosts().first()
        val postEntity = posts.find { it.id == postId }
        
        if (postEntity != null) {
            val updatedLikeCount = postEntity.likeCount + 1
            postDao.updatePost(postEntity.copy(likeCount = updatedLikeCount))
            Log.d("PostRepository", "Updated like count locally for post $postId to $updatedLikeCount")
        } else {
            Log.e("PostRepository", "Post not found: $postId")
        }
    }
}
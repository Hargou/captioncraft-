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
    // Base URL for image paths
    val baseUrl = "http://10.0.2.2:8000"
    
    suspend fun clearCache() {
        postDao.deleteAllPosts()
    }

    fun getFeedPosts(): Flow<List<Post>> = flow {
        // First, try to get posts from local database
        val currentUser = userRepository.currentUser.first() ?: throw IllegalStateException("No user logged in")
        
        // Try to fetch from server first
        try {
            val response = postApi.getAllPosts()
            if (response.isSuccessful && response.body() != null) {
                // Delete all existing posts before inserting new ones
                postDao.deleteAllPosts()
                val postEntities = response.body()!!.data.map { PostDto.fromArray(it).toEntity() }
                postDao.insertPosts(postEntities)
            } else {
                Log.e("PostRepository", "Error fetching posts: ${response.message()}")
            }
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

    suspend fun getPostById(id: Int): Post? {
        try {
            // Unfortunately there's no direct API for getting a post by ID
            // We'll get all posts and filter by ID
            val response = postApi.getAllPosts()
            if (response.isSuccessful && response.body() != null) {
                val posts = response.body()!!.data.map { PostDto.fromArray(it).toDomain() }
                return posts.find { it.id == id }
            }
            return null
        } catch (e: Exception) {
            Log.e("PostRepository", "Error getting post by ID: $id", e)
            return null
        }
    }

    suspend fun getAllPosts(): List<Post> {
        val response = postApi.getAllPosts()
        return if (response.isSuccessful && response.body() != null) {
            response.body()!!.data.map { PostDto.fromArray(it).toDomain() }
        } else {
            emptyList()
        }
    }

    suspend fun getPostsByUser(userId: Int): List<Post> {
        Log.d("PostRepository", "getPostsByUser called for userId: $userId")
        try {
            val response = postApi.getUserPosts(userId)
            Log.d("PostRepository", "API response for getUserPosts($userId): Code=${response.code()}, Success=${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("PostRepository", "getUserPosts($userId) response body: Status=${responseBody.status}, Message=${responseBody.message}, Data=${responseBody.data}")
                val posts = responseBody.data.map { PostDto.fromArray(it).toDomain() }
                Log.d("PostRepository", "Mapped ${posts.size} posts for userId: $userId. Posts: $posts")
                return posts
            } else {
                Log.e("PostRepository", "getUserPosts($userId) failed or body was null. Code: ${response.code()}, Message: ${response.message()}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Exception in getPostsByUser($userId)", e)
            return emptyList()
        }
    }

    suspend fun createPost(userId: Int, imageFile: File, caption: String? = null): Post? {
        try {
            val currentUser = userRepository.currentUser.first() ?: throw IllegalStateException("No user logged in")
            val password = sessionManager.getUserPassword()
            if (password.isEmpty()) {
                throw IllegalStateException("No password available. Please log in again.")
            }
            
            val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())
            val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)
            val captionBody = caption?.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = postApi.createPost(userIdBody, passwordBody, imagePart, captionBody)
            if (response.isSuccessful && response.body() != null) {
                val createResponse = response.body()!!
                Log.d("PostRepository", "Post created successfully: ${createResponse.message}")
                
                // Create a new post object with the returned ID and data
                val newPost = Post(
                    id = createResponse.data.postId,
                    userId = currentUser.id,
                    username = currentUser.username ?: "User ${currentUser.id}",
                    imageUrl = "$baseUrl/post/user_post_images/${createResponse.data.imageName}",
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date()),
                    likeCount = 0,
                    captionCount = if (caption != null) 1 else 0,
                    content = caption ?: "",
                    likes = 0,
                    likedByUser = false
                )
                
                // Save to local database
                postDao.insertPost(newPost.toEntity())
                return newPost
            } else {
                Log.e("PostRepository", "Failed to create post: ${response.message()}")
                return null
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Error creating post", e)
            return null
        }
    }

    suspend fun updatePost(post: Post) {
        // This functionality is not implemented in the backend yet
    }

    suspend fun deletePost(id: Int) {
        // Since deletePost requires a more complex format with userId and password,
        // this functionality is not fully implemented
        // It would require format: "postId_userId_password"
        Log.w("PostRepository", "deletePost is not fully implemented - requires userId and password")
        
        // TODO: Implement this when needed with the proper authentication
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
                if (response.isSuccessful) {
                    Log.d("PostRepository", "Post liked on server: $postId")
                    
                    // Update local database directly
                    updateLocalPostLikeCount(postId)
                } else {
                    Log.e("PostRepository", "Server error liking post: ${response.message()}")
                    // Still update local DB for responsiveness
                    updateLocalPostLikeCount(postId)
                }
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
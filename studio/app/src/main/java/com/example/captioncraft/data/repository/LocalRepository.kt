package com.example.captioncraft.data.repository

import com.example.captioncraft.data.local.entity.CaptionEntity
import com.example.captioncraft.data.local.entity.PostEntity
import com.example.captioncraft.data.local.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class LocalRepository @Inject constructor() {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users.asStateFlow()

    private val _posts = MutableStateFlow<List<PostEntity>>(emptyList())
    val posts: StateFlow<List<PostEntity>> = _posts.asStateFlow()

    fun login(user: UserEntity) {
        Log.d("LocalRepository", "Updating local currentUser state with User ID: ${user.id}, Username: ${user.username}")
        _currentUser.value = user
        _users.update { currentUsers ->
            val existingUserIndex = currentUsers.indexOfFirst { it.id == user.id }
            if (existingUserIndex != -1) {
                currentUsers.toMutableList().apply { set(existingUserIndex, user) }
            } else {
                currentUsers + user
            }
        }
    }

    fun logout() {
        Log.d("LocalRepository", "Clearing local currentUser state.")
        _currentUser.value = null
    }

    fun createPost(userId: Int, imageUrl: String) {
        val post = PostEntity(
            id = 1, // This should be replaced with actual post ID from the database
            userId = userId,
            imageUrl = imageUrl,
            createdAt = Date().toString(),
            likeCount = 0,
            captionCount = 0
        )
        _posts.update { it + post }
    }

    fun addCaption(postId: Int, userId: Int, text: String) {
        val caption = CaptionEntity(
            id = 1, // This should be replaced with actual caption ID from the database
            postId = postId,
            userId = userId,
            text = text,
            createdAt = Date(),
            likes = 0
        )
        // This functionality is handled by the CaptionRepository
    }

    fun toggleCaptionLike(captionId: Int, userId: Int) {
        // This functionality is handled by the CaptionRepository
    }

    fun searchUsers(query: String): List<UserEntity> {
        return _users.value.filter { 
            it.username.contains(query, ignoreCase = true) 
        }
    }

    fun toggleFollow(userId: Int) {
        // This functionality is handled by the FollowRepository
    }

    fun getUserPosts(userId: Int): List<PostEntity> {
        return _posts.value.filter { it.userId == userId }
    }

    fun updateUser(user: UserEntity) {
        _currentUser.value = user
        _users.update { users ->
            users.map { if (it.id == user.id) user else it }
        }
    }
} 
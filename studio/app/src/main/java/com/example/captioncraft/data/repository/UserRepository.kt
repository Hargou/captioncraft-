package com.example.captioncraft.data.repository

import com.bumptech.glide.util.ExceptionPassthroughInputStream
import com.example.captioncraft.data.local.dao.UserDao
import com.example.captioncraft.data.local.entity.UserEntity
import com.example.captioncraft.data.remote.api.UserApi
import com.example.captioncraft.data.remote.dto.LoginDto
import com.example.captioncraft.data.remote.dto.RegisterDto
import com.example.captioncraft.data.remote.dto.UserDto
import com.example.captioncraft.domain.mapper.toDomain
import com.example.captioncraft.domain.mapper.toEntity
import com.example.captioncraft.domain.model.User
import com.example.captioncraft.util.UserSessionManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val sessionManager: UserSessionManager
) {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = userApi.authenticate(LoginDto(
                username = username,
                password = password
            ))
            if (response.status == "green" && response.data != null) {
                val user = response.data.toDomain()
                userDao.insertUser(user.toEntity())
                _currentUser.value = user
                
                // Store password in session for API calls that require authentication
                sessionManager.setUserPassword(password)
                
                Result.success(user)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        _currentUser.value = null
        sessionManager.clearSession() // Clear password when logging out
    }

    suspend fun register(username: String, name: String, password: String): Result<String> {
        return try {
            val response = userApi.register(RegisterDto(username = username, name = name, password = password))
            if (response.status == "green") {
                Result.success(response.message ?: "Registration successful")
            } else if (response.status_code == 400) {
                Result.failure(Exception(response.detail ?: "Registration failed"))
            } else {
                Result.failure(Exception(response.detail ?: response.message ?: "Unknown error occurred"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserEntity): Result<User> {
        return try {
            val response = userApi.updateUser(user.id, UserDto(
                id = user.id,
                username = user.username,
                name = user.name,
                password = "", // This should be handled by the authentication system
                profilePicture = user.profilePicture,
                created_at = user.createdAt.toString()
            ))
            val updatedUser = response?.toDomain() ?: throw Exception("Update failed")
            userDao.updateUser(user)
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserById(id: Int): Flow<User?> {
        return userDao.getUserById(id)
            .map { it?.let { user -> user.toDomain() } }
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
            .map { users -> users.map { it.toDomain() } }
    }

    fun searchUsers(query: String): Flow<List<User>> {
        return userDao.searchUsers(query)
            .map { users -> users.map { it.toDomain() } }
    }
}

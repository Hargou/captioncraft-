package com.example.captioncraft.ui.screens.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.captioncraft.data.repository.PostRepository
import com.example.captioncraft.data.repository.UserRepository
import com.example.captioncraft.domain.model.Post
import com.example.captioncraft.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Int = checkNotNull(savedStateHandle["userId"]) // Get userId from navigation args

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                Log.d("UserProfileViewModel", "Loading profile for userId: $userId")
                // Fetch user details and posts concurrently (or sequentially if dependencies exist)
                val userFlow = userRepository.getUserById(userId)
                val postsFlow = postRepository.getPostsByUser(userId)
                
                // Wait for both to complete (using combine or separate collects)
                val user = userFlow.first() // Assuming getUserById emits once or we only need the first value
                val posts = postsFlow // Keep as flow if it emits updates, or use .first()

                Log.d("UserProfileViewModel", "Fetched user: ${user?.username}, Posts count: ${posts.size}")

                _uiState.update { 
                    it.copy(
                        user = user,
                        posts = posts,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                Log.e("UserProfileViewModel", "Error loading user profile for $userId", e)
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "Failed to load profile",
                        isLoading = false
                    ) 
                }
            }
        }
    }
} 
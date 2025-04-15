package com.example.captioncraft.ui.screens.upload

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.captioncraft.data.repository.LocalRepository
import com.example.captioncraft.data.repository.PostRepository
import com.example.captioncraft.data.repository.UserRepository
import com.example.captioncraft.util.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class UploadUiState(
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun uploadImage(imageFile: File, caption: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = UploadUiState(isUploading = true)
                
                // Get the current user
                val currentUser = userRepository.currentUser.first() 
                    ?: throw IllegalStateException("User not logged in")
                
                // Upload the post
                val createdPost = postRepository.createPost(
                    userId = currentUser.id,
                    imageFile = imageFile,
                    caption = caption
                )
                
                if (createdPost != null) {
                    // Post was created successfully
                    Log.d("UploadViewModel", "Post created successfully with ID: ${createdPost.id}")
                    _uiState.value = UploadUiState(isUploading = false, uploadSuccess = true)
                } else {
                    throw IllegalStateException("Failed to create post - server returned null")
                }
                
            } catch (e: Exception) {
                Log.e("UploadViewModel", "Error uploading image", e)
                _uiState.value = UploadUiState(isUploading = false, errorMessage = e.message)
            }
        }
    }
    
    fun resetState() {
        _uiState.value = UploadUiState()
    }
} 
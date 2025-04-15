package com.example.captioncraft.ui.screens.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.captioncraft.data.repository.LocalRepository
import com.example.captioncraft.data.repository.PostRepository
import com.example.captioncraft.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val localRepository: LocalRepository
) : ViewModel() {
    fun uploadImage(imageUri: Uri, caption: String? = null) {
        viewModelScope.launch {
            val currentUser = localRepository.currentUser.value ?: return@launch
            val imageFile = File(imageUri.path ?: return@launch)
            postRepository.createPost(
                userId = currentUser.id,
                password = "", // This should be handled by the authentication system
                imageFile = imageFile,
                caption = caption
            )
        }
    }
} 
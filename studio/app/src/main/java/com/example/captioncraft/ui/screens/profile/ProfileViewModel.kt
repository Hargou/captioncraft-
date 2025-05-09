package com.example.captioncraft.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.captioncraft.data.local.entity.PostEntity
import com.example.captioncraft.data.local.entity.UserEntity
import com.example.captioncraft.data.repository.FollowRepository
import com.example.captioncraft.data.repository.LocalRepository
import com.example.captioncraft.data.repository.PostRepository
import com.example.captioncraft.data.repository.UserRepository
import com.example.captioncraft.domain.model.Post
import com.example.captioncraft.ui.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val postRepository: PostRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = localRepository.currentUser

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    init {
        viewModelScope.launch {
            currentUser.filterNotNull().collectLatest { user ->
                try {
                    Log.d("ProfileViewModel", "Current user detected: ID=${user.id}, Username=${user.username}")
                    Log.d("ProfileViewModel", "Fetching posts for user ID: ${user.id}")
                    val posts = postRepository.getPostsByUser(user.id)
                    Log.d("ProfileViewModel", "Fetched ${posts.size} posts for user ID: ${user.id}. Posts: $posts")
                    _userPosts.value = posts
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching user posts for user ID: ${user.id}", e)
                    _userPosts.value = emptyList()
                }
            }
        }
    }

    // Temporarily set to 0 until we implement these methods in FollowRepository
    val followerCount: StateFlow<Int> = MutableStateFlow(0)
    val followingCount: StateFlow<Int> = MutableStateFlow(0)

    private val _editImageUri = MutableStateFlow<Uri?>(null)
    val editImageUri: StateFlow<Uri?> = _editImageUri

    private val _editedUsername = MutableStateFlow("")
    val editedUsername: StateFlow<String> = _editedUsername

    fun onImagePicked(uri: Uri?) {
        _editImageUri.value = uri
    }

    fun onUsernameChanged(newUsername: String) {
        _editedUsername.value = newUsername
    }

    fun enterEditMode() {
        _editedUsername.value = currentUser.value?.username ?: ""
        _editImageUri.value = null
    }

    fun updateProfile() {
        val updatedUsername = _editedUsername.value
        val updatedProfilePicture = _editImageUri.value?.toString()

        val current = currentUser.value ?: return

        val updatedUser = current.copy(
            username = updatedUsername,
            profilePicture = updatedProfilePicture ?: current.profilePicture
        )

        viewModelScope.launch {
            userRepository.updateUser(updatedUser)
            localRepository.updateUser(updatedUser)
        }

        _editImageUri.value = null
        _editedUsername.value = ""
    }

    fun logout(navController: NavHostController) {
        navController.navigate(Screen.Login.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        localRepository.logout()
    }

    fun navigateToSettings(navController: NavHostController) {
        navController.navigate(Screen.Settings.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

package com.example.captioncraft.ui.screens.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.captioncraft.data.local.entity.PostEntity
import com.example.captioncraft.data.repository.CaptionRepository
import com.example.captioncraft.data.repository.PostRepository
import com.example.captioncraft.data.repository.UserRepository
import com.example.captioncraft.domain.model.Caption
import com.example.captioncraft.domain.model.Comment
import com.example.captioncraft.domain.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val commentsForCaption: Map<Int, List<Comment>> = emptyMap(),
    val showCommentsForCaption: Int? = null,
    val likedCaptions: MutableSet<Int> = mutableSetOf(), // Track liked captions locally
    val cachedPostCaptions: Map<Int, List<Caption>> = emptyMap() // Cache for post captions
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val captionRepository: CaptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Get ONLY the first/latest emission from the repository Flow
                val posts = postRepository.getFeedPosts().first()
                Log.d("FeedViewModel", "Got ${posts.size} posts from repository (using .first())")

                val cachedCaptions = _uiState.value.cachedPostCaptions.toMutableMap()
                val postsWithCaptions = mutableListOf<Post>()

                for (post in posts) {
                    try {
                        // Check cache first
                        val captions = if (cachedCaptions.containsKey(post.id)) {
                            Log.d("FeedViewModel", "Using cached captions for post ${post.id}")
                            cachedCaptions[post.id] ?: emptyList()
                        } else {
                            // Fetch captions if not cached
                            Log.d("FeedViewModel", "Fetching captions for post ${post.id} from repository")
                            try {
                                // Use .first() here too for safety, assuming getCaptionsForPost emits the full list once
                                val fetchedCaptions = captionRepository.getCaptionsForPost(post.id).first()
                                Log.d("FeedViewModel", "Successfully loaded ${fetchedCaptions.size} captions for post ${post.id}")
                                
                                // Debug each fetched caption
                                fetchedCaptions.forEachIndexed { index, caption ->
                                    Log.d("FeedViewModel", "Fetched Caption for Post ${post.id} #$index: id=${caption.id}, text='${caption.text}', username=${caption.username}")
                                }
                                
                                // Cache the captions
                                cachedCaptions[post.id] = fetchedCaptions
                                fetchedCaptions
                            } catch (e: Exception) {
                                Log.e("FeedViewModel", "Error loading captions from repository for post ${post.id}", e)
                                emptyList()
                            }
                        }
                        
                        // Create the updated Post object
                        val updatedPost = post.copy(
                            captions = captions,
                            captionCount = captions.size.coerceAtLeast(post.captionCount)
                        )
                        
                        Log.d("FeedViewModel", "Processed post ${updatedPost.id} - Captions: ${updatedPost.captions.size}, Count: ${updatedPost.captionCount}")
                        postsWithCaptions.add(updatedPost)
                        
                    } catch (e: Exception) {
                        Log.e("FeedViewModel", "Error processing post ${post.id} during caption loading", e)
                        // Add the original post without captions if processing failed
                        postsWithCaptions.add(post.copy(captions = emptyList())) 
                    }
                }

                Log.d("FeedViewModel", "Finished processing all posts. Total posts with captions: ${postsWithCaptions.size}")
                
                // Update the UI state ONCE with the final list
                _uiState.update { 
                    it.copy(
                        posts = postsWithCaptions,
                        isLoading = false,
                        cachedPostCaptions = cachedCaptions // Update cache in state
                    )
                }
                
                // Final debug log of the state being set
                postsWithCaptions.forEach { finalPost ->
                    Log.d("FeedViewModel", "Final UI State - Post #${finalPost.id}: Captions Size=${finalPost.captions.size}, CaptionCount=${finalPost.captionCount}")
                }

            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error in loadFeed top level", e)
                _uiState.update { it.copy(error = e.message ?: "Failed to load feed", isLoading = false) }
            }
        }
    }

    fun refreshFeed() {
        loadFeed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val feedPosts: StateFlow<List<Post>> = userRepository.currentUser
        .filterNotNull()
        .flatMapLatest { _ -> 
            postRepository.getFeedPosts()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun addCaption(postId: Int, text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) } // Show loading indicator
            try {
                val userId = userRepository.currentUser.value?.id ?: run {
                    _uiState.update { it.copy(error = "User not logged in", isLoading = false) }
                    return@launch
                }
                
                val result = captionRepository.addCaption(postId, userId, text)
                result.onSuccess { captionId ->
                    // Successfully added caption, refresh feed to get the new caption
                    Log.d("FeedViewModel", "Caption added successfully (ID: $captionId), refreshing feed.")
                    loadFeed() 
                }
                result.onFailure { error ->
                    Log.e("FeedViewModel", "Failed to add caption", error)
                    _uiState.update { it.copy(error = error.message ?: "Failed to add caption", isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Exception while adding caption", e)
                _uiState.update { it.copy(error = e.message ?: "An error occurred", isLoading = false) }
            }
        }
    }

    // Refactored toggleLike for captions
    fun toggleLike(captionId: Int) {
        viewModelScope.launch {
            val currentUiState = _uiState.value
            val userId = userRepository.currentUser.value?.id ?: run {
                _uiState.update { it.copy(error = "User not logged in") }
                return@launch
            }

            // Optimistically update the UI first
            val isCurrentlyLiked = currentUiState.likedCaptions.contains(captionId)
            val updatedLikedCaptions = currentUiState.likedCaptions.toMutableSet()
            if (isCurrentlyLiked) {
                updatedLikedCaptions.remove(captionId)
            } else {
                updatedLikedCaptions.add(captionId)
            }

            // Find the post and caption to update the like count visually
            val updatedPosts = currentUiState.posts.map { post ->
                val captionIndex = post.captions.indexOfFirst { it.id == captionId }
                if (captionIndex != -1) {
                    val caption = post.captions[captionIndex]
                    val updatedLikeCount = if (isCurrentlyLiked) caption.likes - 1 else caption.likes + 1
                    val updatedCaption = caption.copy(likes = updatedLikeCount.coerceAtLeast(0))
                    val updatedCaptionsList = post.captions.toMutableList().apply { set(captionIndex, updatedCaption) }
                    post.copy(captions = updatedCaptionsList)
                } else {
                    post
                }
            }
            
            // Apply optimistic UI update
            _uiState.update { 
                it.copy(
                    likedCaptions = updatedLikedCaptions,
                    posts = updatedPosts
                )
            }
            Log.d("FeedViewModel", "Optimistically updated UI for caption like: $captionId, Liked: ${!isCurrentlyLiked}")

            // Now, call the repository to update the backend
            try {
                captionRepository.toggleLike(captionId, userId, isCurrentlyLiked)
                Log.d("FeedViewModel", "Successfully toggled like on backend for caption $captionId")
                // Optional: Could refresh just this post's captions from server if needed,
                // but optimistic update might be enough for good UX.
                // loadFeed() // Avoid full reload if optimistic update is sufficient
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error toggling like on backend for caption $captionId", e)
                // Revert optimistic UI update on error
                _uiState.update { 
                    it.copy(
                        likedCaptions = currentUiState.likedCaptions, // Revert liked set
                        posts = currentUiState.posts, // Revert posts list
                        error = e.message ?: "Failed to like caption"
                    )
                 }
            }
        }
    }

    fun getCaptions(postId: Int): Flow<List<Caption>> = captionRepository.getCaptionsForPost(postId)

    fun togglePostLike(postId: Int) {
        viewModelScope.launch {
            try {
                postRepository.toggleLike(postId)
                loadFeed()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // Comment methods
    fun loadCommentsForCaption(captionId: Int) {
        viewModelScope.launch {
            try {
                val result = captionRepository.getComments(captionId)
                result.onSuccess { comments ->
                    _uiState.update { 
                        it.copy(
                            commentsForCaption = it.commentsForCaption + (captionId to comments),
                            showCommentsForCaption = captionId
                        )
                    }
                }
                result.onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addComment(captionId: Int, text: String) {
        viewModelScope.launch {
            try {
                val userId = userRepository.currentUser.value?.id ?: return@launch
                val result = captionRepository.addComment(captionId, userId, text)
                result.onSuccess { comment ->
                    val currentComments = _uiState.value.commentsForCaption[captionId] ?: emptyList()
                    val updatedComments = currentComments + comment
                    _uiState.update {
                        it.copy(commentsForCaption = it.commentsForCaption + (captionId to updatedComments))
                    }
                }
                result.onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun hideComments() {
        _uiState.update { it.copy(showCommentsForCaption = null) }
    }
} 
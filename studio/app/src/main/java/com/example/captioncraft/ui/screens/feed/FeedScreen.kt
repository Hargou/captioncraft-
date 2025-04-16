package com.example.captioncraft.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.captioncraft.R
import com.example.captioncraft.domain.model.Post
import com.example.captioncraft.domain.model.Caption
import com.example.captioncraft.domain.model.Comment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    onPostClick: (Int) -> Unit,
    onAddCaptionClick: (Int) -> Unit,
    onNavigateToAddPost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // State for managing the add caption dialog
    var showAddCaptionDialog by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    
    // State for managing the caption screen
    var showCaptionsScreen by remember { mutableStateOf(false) }
    var selectedPostForCaptions by remember { mutableStateOf<Post?>(null) }
    
    LaunchedEffect(key1 = true) {
        viewModel.loadFeed()
    }
    
    // Show error message if present
    LaunchedEffect(uiState.error) {
        if (!uiState.error.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "An error occurred",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Show add caption dialog if needed
    if (showAddCaptionDialog && selectedPostId != null) {
        AddCaptionDialog(
            onDismiss = { 
                showAddCaptionDialog = false
                selectedPostId = null
            },
            onSubmit = { caption ->
                selectedPostId?.let { postId ->
                    viewModel.addCaption(postId, caption)
                }
                showAddCaptionDialog = false
                selectedPostId = null
            }
        )
    }
    
    // Show captions screen if needed
    if (showCaptionsScreen && selectedPostForCaptions != null) {
        // Find the *current* post from the UI state to ensure it reflects updates
        val currentPostInState = uiState.posts.find { it.id == selectedPostForCaptions!!.id }
        
        // Only show the dialog if the post is still found in the current state
        currentPostInState?.let { postToShow ->
            CaptionsScreen(
                post = postToShow, // Pass the up-to-date post from the state
                onDismiss = {
                    showCaptionsScreen = false
                    selectedPostForCaptions = null
                },
                onLikeCaption = { captionId ->
                    viewModel.toggleLike(captionId)
                },
                onAddComment = { captionId, text ->
                    viewModel.addComment(captionId, text)
                },
                commentsForCaption = uiState.commentsForCaption,
                showCommentsForCaption = uiState.showCommentsForCaption,
                onShowComments = { captionId ->
                    viewModel.loadCommentsForCaption(captionId)
                },
                onHideComments = {
                    viewModel.hideComments()
                }
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPost
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Post"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.posts.isEmpty() && uiState.error.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No posts yet",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Be the first to share something interesting!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.posts) { post ->
                        PostCard(
                            post = post,
                            onLikePost = { viewModel.togglePostLike(it) },
                            onCaptionClick = { postId ->
                                // Find the post with this ID and show captions screen
                                val postToView = uiState.posts.find { it.id == postId }
                                if (postToView != null) {
                                    selectedPostForCaptions = postToView
                                    showCaptionsScreen = true
                                }
                            },
                            onAddCaptionClick = { postId ->
                                selectedPostId = postId
                                showAddCaptionDialog = true
                            },
                            onLikeCaption = { captionId ->
                                viewModel.toggleLike(captionId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    onLikePost: (Int) -> Unit,
    onCaptionClick: (Int) -> Unit,
    onAddCaptionClick: (Int) -> Unit,
    onLikeCaption: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Debug log to check post captions
    LaunchedEffect(post.id) {
        Log.d("PostCard", "Post #${post.id} has ${post.captions.size} captions and captionCount=${post.captionCount}")
        if (post.captions.isNotEmpty()) {
            post.captions.forEachIndexed { index, caption ->
                Log.d("PostCard", "Caption #$index: id=${caption.id}, text='${caption.text}', username=${caption.username}")
            }
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Post author username at the top in large, bold text
            Text(
                text = post.username,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // User info row with profile picture
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Profile picture with circle shape
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Post metadata
                Column {
                    Text(
                        text = "Post #${post.id}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Posted on ${post.createdAt.substring(0, 10)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Post image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.placeholder_image),
                placeholder = painterResource(id = R.drawable.placeholder_image)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            // Like button and count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onLikePost(post.id) }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like Post",
                        tint = if (post.likedByUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${post.likes} likes",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Top caption or "No captions yet" message
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                if (post.captions.isNotEmpty()) {
                    val topCaption = post.captions.first()
                    Log.d("PostCard", "Displaying top caption: id=${topCaption.id}, text='${topCaption.text}', username=${topCaption.username}")
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = topCaption.username,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topCaption.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = topCaption.likes.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            IconButton(
                                onClick = { onLikeCaption(topCaption.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = "Like Caption",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No captions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (post.captionCount > 0 || post.captions.isNotEmpty()) {
                    Button(
                        onClick = { onCaptionClick(post.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View Captions (${post.captions.size.coerceAtLeast(post.captionCount)})")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                Button(
                    onClick = { onAddCaptionClick(post.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Caption")
                }
            }
        }
    }
}

@Composable
fun CaptionItem(
    caption: Caption,
    onLike: () -> Unit,
    onViewComments: () -> Unit,
    isShowingComments: Boolean
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .border(
                width = 1.dp,
                color = if (isShowingComments) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = caption.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = caption.likes.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
                IconButton(
                    onClick = onLike,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = stringResource(R.string.like),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSection(
    captionId: Int,
    comments: List<Comment>,
    onAddComment: (Int, String) -> Unit,
    onClose: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close comments"
                    )
                }
            }
            
            Divider()
            
            // List of comments
            if (comments.isEmpty()) {
                Text(
                    text = "No comments yet. Be the first to comment!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comments.forEach { comment ->
                        CommentItem(comment = comment)
                    }
                }
            }
            
            Divider()
            
            // Add comment field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...") },
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onAddComment(captionId, commentText)
                            commentText = ""
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send comment"
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Helper function to format date
private fun formatDate(date: java.util.Date?): String {
    // If date is null, return a default string
    if (date == null) return "unknown time"
    
    // Simple formatter that returns relative time like "2h ago", "5m ago", etc.
    val now = java.util.Date()
    val diffInMillis = now.time - date.time
    val diffInSeconds = diffInMillis / 1000
    
    return when {
        diffInSeconds < 60 -> "just now"
        diffInSeconds < 3600 -> "${diffInSeconds / 60}m ago"
        diffInSeconds < 86400 -> "${diffInSeconds / 3600}h ago"
        else -> "${diffInSeconds / 86400}d ago"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCaptionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var captionText by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Caption") },
        text = {
            Column {
                Text(
                    "Write a creative caption for this post!",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Enter your caption...") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (captionText.isBlank()) {
                        Toast.makeText(context, "Caption cannot be empty", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmit(captionText)
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CaptionsScreen(
    post: Post,
    onDismiss: () -> Unit,
    onLikeCaption: (Int) -> Unit,
    onAddComment: (Int, String) -> Unit,
    commentsForCaption: Map<Int, List<Comment>>,
    showCommentsForCaption: Int?,
    onShowComments: (Int) -> Unit,
    onHideComments: () -> Unit
) {
    // Debug log
    LaunchedEffect(post.id) {
        Log.d("CaptionsScreen", "Displaying captions for post #${post.id}")
        Log.d("CaptionsScreen", "Post has ${post.captions.size} captions and captionCount=${post.captionCount}")
        post.captions.forEachIndexed { index, caption ->
            Log.d("CaptionsScreen", "Caption #$index: id=${caption.id}, text='${caption.text}', username=${caption.username}")
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        title = { Text("Captions for Post #${post.id} (${post.captions.size})") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Post image thumbnail
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.placeholder_image),
                    placeholder = painterResource(id = R.drawable.placeholder_image)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (post.captions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No captions available for this post.")
                    }
                } else {
                    LazyColumn {
                        items(post.captions) { caption ->
                            val isShowingComments = showCommentsForCaption == caption.id
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                if (isShowingComments) {
                                                    onHideComments()
                                                } else {
                                                    onShowComments(caption.id)
                                                }
                                            },
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = caption.username,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = caption.likes.toString(),
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                    IconButton(
                                                        onClick = { onLikeCaption(caption.id) },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ThumbUp,
                                                            contentDescription = "Like",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Text(
                                                text = caption.text,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                                
                                // Show comments if selected
                                if (isShowingComments) {
                                    val comments = commentsForCaption[caption.id] ?: emptyList()
                                    CommentSection(
                                        captionId = caption.id,
                                        comments = comments,
                                        onAddComment = onAddComment,
                                        onClose = onHideComments
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
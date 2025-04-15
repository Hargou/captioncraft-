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
import com.example.captioncraft.R
import com.example.captioncraft.domain.model.Post
import com.example.captioncraft.domain.model.Caption
import com.example.captioncraft.domain.model.Comment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.ui.text.style.TextOverflow
import android.util.Log

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        !uiState.error.isNullOrEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.posts) { post ->
                    PostCard(
                        post = post,
                        onAddCaption = { text -> viewModel.addCaption(post.id, text) },
                        onLikeCaption = { captionId -> viewModel.toggleLike(captionId) },
                        onLikePost = { viewModel.togglePostLike(post.id) },
                        onViewComments = { captionId -> viewModel.loadCommentsForCaption(captionId) },
                        onAddComment = { captionId, text -> viewModel.addComment(captionId, text) },
                        comments = uiState.commentsForCaption[uiState.showCommentsForCaption] ?: emptyList(),
                        showCommentsForCaptionId = uiState.showCommentsForCaption,
                        onHideComments = { viewModel.hideComments() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    onAddCaption: (String) -> Unit,
    onLikeCaption: (Int) -> Unit,
    onLikePost: () -> Unit,
    onViewComments: (Int) -> Unit,
    onAddComment: (Int, String) -> Unit,
    comments: List<Comment>,
    showCommentsForCaptionId: Int?,
    onHideComments: () -> Unit
) {
    var showAddCaption by remember { mutableStateOf(false) }
    var captionText by remember { mutableStateOf("") }
    
    // Debug log to check captions
    Log.d("FeedScreen", "PostCard for post ID ${post.id} has ${post.captions.size} captions")
    post.captions.forEach { caption ->
        Log.d("FeedScreen", "Caption ID: ${caption.id}, Text: ${caption.text}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Post image
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.placeholder_image)
            )

            // Post stats and like button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onLikePost) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = stringResource(R.string.like),
                            tint = if (post.likeCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = post.likeCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (post.likeCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Captions: ${post.captionCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Add caption button/field
            if (showAddCaption) {
                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.add_caption)) }
                )
                Button(
                    onClick = {
                        if (captionText.isNotBlank()) {
                            onAddCaption(captionText)
                            captionText = ""
                            showAddCaption = false
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.add_caption))
                }
            } else {
                OutlinedButton(
                    onClick = { showAddCaption = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_caption))
                }
            }
            
            // Captions Section with debug
            if (post.captions.isEmpty()) {
                Text(
                    text = "No captions yet. Add one!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Text(
                    text = "Captions (${post.captions.size}):",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    lazyRowItems(post.captions) { caption ->
                        CaptionItem(
                            caption = caption,
                            onLike = { onLikeCaption(caption.id) },
                            onViewComments = { onViewComments(caption.id) },
                            isShowingComments = showCommentsForCaptionId == caption.id
                        )
                    }
                }
            }
            
            // Comments Section (shown only when a caption is selected)
            if (showCommentsForCaptionId != null) {
                CommentSection(
                    captionId = showCommentsForCaptionId,
                    comments = comments,
                    onAddComment = onAddComment,
                    onClose = onHideComments
                )
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
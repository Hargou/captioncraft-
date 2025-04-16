package com.example.captioncraft.ui.screens.profile

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person // For placeholder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.captioncraft.R
import com.example.captioncraft.domain.model.Post

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val posts = uiState.posts

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.username ?: "User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                // Check isLoading is false before showing user not found
                !uiState.isLoading && user == null -> {
                     Text(
                        text = "User not found.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                user != null -> {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        // User Info Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.CenterVertically) // Align the box itself
                                    .background(MaterialTheme.colorScheme.primaryContainer) // BG for placeholder
                            ) {
                                val displayImage = user.profilePicture?.let { Uri.parse(it) }
                                if (displayImage != null) {
                                    GlideImage(
                                        model = displayImage,
                                        contentDescription = "Profile Image",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Placeholder Icon if no image
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile Image Placeholder",
                                        modifier = Modifier.fillMaxSize().padding(16.dp), // Add padding
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer // Adjust tint
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(text = user.username, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Use the ProfileStat composable defined below
                                ProfileStat("Posts", posts.size)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("${user.username}'s Posts", style = MaterialTheme.typography.titleMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Posts Grid
                        if (posts.isEmpty()) {
                             Text(
                                text = "This user hasn't posted anything yet.",
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                             LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(), // Fill remaining space
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(top = 8.dp) // Add some top padding
                             ) {
                                // Use the PostThumbnail composable defined below
                                items(posts) { post -> PostThumbnail(post) }
                             }
                        }
                    }
                }
            }
        }
    }
}

// Define helper composables needed by this screen

@Composable
fun ProfileStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.bodyLarge)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PostThumbnail(post: Post) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f) // Ensure square aspect ratio
            .fillMaxWidth(),
        shape = RoundedCornerShape(4.dp), // Optional: add slight rounding
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Post Thumbnail for post ${post.id}",
            contentScale = ContentScale.Crop, // Crop to fit the square
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.placeholder_image),
            placeholder = painterResource(id = R.drawable.placeholder_image)
        )
    }
} 
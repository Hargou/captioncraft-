package com.example.captioncraft.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.captioncraft.data.local.entity.PostEntity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.draw.clip
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userPosts by viewModel.userPosts.collectAsState()
    val imageUri by viewModel.editImageUri.collectAsState()
    val editedUsername by viewModel.editedUsername.collectAsState()
    val followerCount by viewModel.followerCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> viewModel.onImagePicked(uri) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = currentUser?.username ?: "", style = MaterialTheme.typography.titleMedium)
            Row {
                IconButton(onClick = { viewModel.navigateToSettings(navController) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = { viewModel.logout(navController) }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable(enabled = isEditing) {
                        if (isEditing) imagePickerLauncher.launch("image/*")
                    }
                    .border(width = 4.dp, color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
            ) {
                val displayImage = imageUri ?: currentUser?.profilePicture?.let { Uri.parse(it) }
                displayImage?.let {
                    GlideImage(
                        model = it,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                if (isEditing) {
                    OutlinedTextField(
                        value = editedUsername,
                        onValueChange = viewModel::onUsernameChanged,
                        label = { Text("Username") },
                        singleLine = true
                    )
                } else {
                    Text(text = currentUser?.username ?: "", style = MaterialTheme.typography.titleLarge)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileStat("Posts", userPosts.size)
                    ProfileStat("Followers", followerCount)
                    ProfileStat("Following", followingCount)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isEditing) viewModel.updateProfile()
                else viewModel.enterEditMode()
                isEditing = !isEditing
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isEditing) "Save Changes" else "Edit Profile")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("My Posts", style = MaterialTheme.typography.titleMedium)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(userPosts) { post -> PostThumbnail(post) }
        }
    }
}

@Composable
fun ProfileStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.bodyLarge)
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PostThumbnail(post: PostEntity) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Post Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

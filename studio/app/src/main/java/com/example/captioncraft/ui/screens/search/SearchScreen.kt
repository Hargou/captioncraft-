package com.example.captioncraft.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.captioncraft.R
import com.example.captioncraft.data.local.entity.UserEntity
import androidx.compose.foundation.shape.CircleShape

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_users)) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults) { user ->
                UserListItem(
                    user = user,
                    onFollowClick = { viewModel.toggleFollow(user.id) }
                )
            }
        }
    }
}

@Composable
fun UserListItem(
    user: UserEntity,
    onFollowClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(user.username) },
        leadingContent = {
            // User avatar placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
        },
        trailingContent = {
            IconButton(onClick = onFollowClick) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = stringResource(R.string.follow)
                )
            }
        }
    )
}

// This is a placeholder data class - we'll implement the actual data model later
data class User(
    val id: String,
    val username: String,
    val avatarUrl: String?
) 
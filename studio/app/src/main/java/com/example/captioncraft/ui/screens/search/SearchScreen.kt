package com.example.captioncraft.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.captioncraft.R
import com.example.captioncraft.data.local.entity.UserEntity
import com.example.captioncraft.ui.Screen
import androidx.compose.foundation.shape.CircleShape

@Composable
fun SearchScreen(
    navController: NavController,
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
                    onUserClick = { 
                        navController.navigate(Screen.UserProfile.createRoute(user.id))
                    }
                )
            }
        }
    }
}

@Composable
fun UserListItem(
    user: UserEntity,
    onUserClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onUserClick),
        headlineContent = { Text(user.username) },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                 Icon(
                    Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
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
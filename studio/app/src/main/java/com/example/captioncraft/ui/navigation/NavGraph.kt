package com.example.captioncraft.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.captioncraft.ui.screens.feed.FeedScreen
import com.example.captioncraft.ui.screens.login.LoginScreen
import com.example.captioncraft.ui.screens.profile.ProfileScreen
import com.example.captioncraft.ui.screens.search.SearchScreen
import com.example.captioncraft.ui.screens.upload.UploadScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        
        composable("feed") {
            FeedScreen(
                onPostClick = { /* Handle post click */ },
                onAddCaptionClick = { /* Handle add caption click */ },
                onNavigateToAddPost = { /* Handle navigate to add post */ }
            )
        }
        
        composable("profile") {
            ProfileScreen(navController = navController)
        }
        
        composable("search") {
            SearchScreen()
        }
        
        composable("upload") {
            UploadScreen()
        }
    }
} 
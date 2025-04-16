package com.example.captioncraft.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.captioncraft.R
import com.example.captioncraft.ui.screens.feed.FeedScreen
import com.example.captioncraft.ui.screens.login.LoginScreen
import com.example.captioncraft.ui.screens.profile.ProfileScreen
import com.example.captioncraft.ui.screens.search.SearchScreen
import com.example.captioncraft.ui.screens.settings.SettingsScreen
import com.example.captioncraft.ui.screens.upload.UploadScreen
import com.example.captioncraft.ui.screens.profile.UserProfileScreen

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Search : Screen("search")
    object Upload : Screen("upload")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Login : Screen("login")
    object UserProfile : Screen("userProfile/{userId}") {
        fun createRoute(userId: Int) = "userProfile/$userId"
    }
}

@Composable
fun CaptionCraftApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items = listOf(
        Triple(Screen.Feed, Icons.Default.Home, R.string.feed),
        Triple(Screen.Search, Icons.Default.Search, R.string.search),
        Triple(Screen.Upload, Icons.Default.Add, R.string.create_post),
        Triple(Screen.Profile, Icons.Default.Person, R.string.profile)
    )

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != Screen.Login.route) {
                NavigationBar {
                    items.forEach { (screen, icon, labelResId) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(stringResource(labelResId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) { 
                FeedScreen(
                    onPostClick = { postId -> 
                        // You can navigate to post details screen here
                    },
                    onAddCaptionClick = { /* No-op, handled internally */ },
                    onNavigateToAddPost = {
                        navController.navigate(Screen.Upload.route)
                    }
                ) 
            }
            composable(Screen.Search.route) { 
                SearchScreen(navController = navController)
            }
            composable(Screen.Upload.route) { UploadScreen() }
            composable(Screen.Profile.route) { ProfileScreen(navController = navController) }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.Login.route) { LoginScreen(navController = navController) }
            composable(
                route = Screen.UserProfile.route, 
                arguments = listOf(navArgument("userId") { type = NavType.IntType })
            ) {
                UserProfileScreen(navController = navController)
            }
        }
    }
} 
package com.example.captioncraft.ui.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkTheme by viewModel.isDarkMode.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val isPrivateModeEnabled by viewModel.isPrivateModeEnabled.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }

        item {
            SettingSwitch(
                label = "Dark Mode",
                checked = isDarkTheme,
                onCheckedChange = viewModel::toggleDarkMode
            )
        }

        item {
            SettingSwitch(
                label = "Notifications",
                checked = isNotificationsEnabled,
                onCheckedChange = viewModel::toggleNotifications
            )
        }
        item {
            SettingSwitch(
                label = "Private Mode",
                checked = isPrivateModeEnabled,
                onCheckedChange = viewModel::togglePrivateMode
            )
        }
    }
}
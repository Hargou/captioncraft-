package com.example.captioncraft.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HealthScreen(
    viewModel: HealthViewModel = hiltViewModel()
) {
    val healthState by viewModel.healthState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkHealth()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (healthState) {
            is HealthState.Loading -> {
                CircularProgressIndicator()
            }
            is HealthState.Success -> {
                val response = (healthState as HealthState.Success).response
                Text(
                    text = "Status: ${response.status}",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = response.message,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version: ${response.version}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is HealthState.Error -> {
                Text(
                    text = (healthState as HealthState.Error).message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 
package com.tomatomediacenter.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button // Import Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tomatomediacenter.ui.MediaPlayerScreen // Import MediaPlayerScreen

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object MediaPlayerScreen : Screen("media_player_screen") // Added MediaPlayerScreen
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            HomeScreen(onNavigateToMediaPlayer = { navController.navigate(Screen.MediaPlayerScreen.route) }) // Pass navigation lambda
        }
        composable(Screen.MediaPlayerScreen.route) { // Added route for MediaPlayerScreen
            MediaPlayerScreen()
        }
        // Add other composables/screens here
    }
}

@Composable
fun HomeScreen(onNavigateToMediaPlayer: () -> Unit) { // Modified HomeScreen to accept lambda
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Home Screen")
        Button(onClick = onNavigateToMediaPlayer) { // Added Button
            Text("Go to Media Player")
        }
    }
}

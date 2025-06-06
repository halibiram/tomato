package com.tomatomediacenter.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.HomeScreen.route) {
        composable(Screen.HomeScreen.route) {
            HomeScreen()
        }
        // Add other composables/screens here
    }
}

@Composable
fun HomeScreen() {
    // Placeholder HomeScreen content
    Column(
        modifier = Modifier.fillMaxSize(),
        // verticalArrangement = Arrangement.Center, // Removed
        // horizontalAlignment = Alignment.CenterHorizontally // Removed
    ) {
        Text(text = "Welcome to the Home Screen!")
        Text(text = "Item 1")
        Text(text = "Item 2")
        Text(text = "Item 3")
    }
}

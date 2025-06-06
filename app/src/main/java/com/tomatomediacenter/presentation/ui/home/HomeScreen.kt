package com.tomatomediacenter.presentation.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPlayer: (String) -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val items = List(20) { "Item ${'$'}it" } // Placeholder data

    // TODO: Use the navigation callbacks, e.g., on buttons or list item clicks.
    // For now, they are just accepted as parameters.

    Column {
        Text(
            text = "Content Discovery",
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(items) { item ->
                Text(
                    text = item,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HomeScreen(
        onNavigateToSearch = { println("Preview: Navigate to Search") },
        onNavigateToPlayer = { movieId -> println("Preview: Navigate to Player with $movieId") },
        onNavigateToDownloads = { println("Preview: Navigate to Downloads") },
        onNavigateToSettings = { println("Preview: Navigate to Settings") }
    )
}

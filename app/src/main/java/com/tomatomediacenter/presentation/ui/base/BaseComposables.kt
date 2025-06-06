package com.tomatomediacenter.presentation.ui.base

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
// For HiltViewModel example:
// import androidx.hilt.navigation.compose.hiltViewModel
// import com.tomatomediacenter.presentation.viewmodels.HomeViewModel // Assuming HomeViewModel exists
// import com.tomatomediacenter.presentation.viewmodels.HomeEvent // Assuming HomeEvent exists

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This file provides a set of reusable, standardized Composable functions for common UI states
 *    like loading, error, and a base screen wrapper. Using these base composables ensures a
 *    consistent look and feel for these states across all screens in the application.
 *    It also reduces boilerplate code in individual screen implementations.
 *
 * 2. How developers should use it:
 *    - `LoadingScreen`: Use when the entire screen's data is loading.
 *    - `ErrorScreen`: Use to display a full-screen error state with a retry option.
 *    - `BaseScreen`: This is the primary wrapper for most screens. It takes the current UI state,
 *      loading status, and error status (typically from a `BaseViewModel`) and automatically
 *      displays the appropriate `LoadingScreen`, `ErrorScreen`, or the screen's main content.
 *      Developers provide their screen-specific content as a lambda to `BaseScreen`.
 *
 * 3. What NOT to do:
 *    - Avoid re-implementing custom loading or error indicators for every screen if these
 *      base components suffice.
 *    - Don't embed complex screen-specific logic within these base composables. They should
 *      remain generic and reusable.
 *
 * 4. Common pitfalls to avoid:
 *    - Incorrectly passing loading/error states to `BaseScreen`, leading to it not displaying
 *      the correct UI.
 *    - Forgetting to provide an `onRetry` lambda to `ErrorScreen` or `BaseScreen`, leaving the
 *      user stuck on an error.
 *    - Styling these components directly in a way that makes them inconsistent with the
 *      overall app theme. Theme adjustments should be done via `MaterialTheme`.
 *
 * 5. Integration with other components:
 *    - `BaseScreen` is designed to work seamlessly with `BaseViewModel`, consuming its
 *      `uiState`, `isLoading`, and `error` StateFlows.
 *    - These composables use components from `androidx.compose.material3`.
 *    - The `onRetry` lambda in `ErrorScreen` typically dispatches an event back to the
 *      ViewModel to re-trigger the data loading operation.
 *
 * 6. Testing strategies:
 *    - Use `@Preview` composables to visually inspect `LoadingScreen` and `ErrorScreen`.
 *    - For `BaseScreen`, create previews that simulate different states (loading, error, success).
 *    - In UI tests (Espresso or Compose testing library), verify that the correct composable
 *      (Loading, Error, or content) is displayed based on the ViewModel's state.
 */

/**
 * Standard loading state for all screens or large components.
 * Displays a centered circular progress indicator and an optional message.
 *
 * @param modifier Modifier for this composable.
 * @param message A custom message to display below the progress indicator.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Loading..." // Default message
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Add some padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp) // Slightly larger indicator
            )
            if (message.isNotBlank()) { // Only show text if message is not blank
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium, // More prominent text
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Good contrast
                )
            }
        }
    }
}

/**
 * Standard error state for all screens or large components.
 * Displays an error message and a retry button.
 *
 * @param error The error message to display.
 * @param onRetry A lambda function to be invoked when the retry button is clicked.
 * @param modifier Modifier for this composable.
 */
@Composable
fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Something went wrong", // Generic title
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error // Use error color for title
            )
            Text(
                text = error, // Specific error message
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer // Good contrast on error container
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * Standard screen wrapper with loading and error handling.
 * ALL DEVELOPERS SHOULD USE THIS for their screen's root composable.
 * It observes common state flags (isLoading, error) and displays either
 * a loading indicator, an error message, or the main screen content.
 *
 * @param T The type of the UI state.
 * @param uiState The current UI state object.
 * @param isLoading Boolean flag indicating if data is currently loading.
 * @param error An optional error message string. If not null, the ErrorScreen is displayed.
 * @param onRetry A lambda function to be invoked for retrying after an error.
 * @param modifier Modifier for this composable.
 * @param loadingMessage Custom message for the loading screen.
 * @param content The main content of the screen, displayed when not loading and no error.
 *                It's a composable lambda that receives the `uiState`.
 */
@Composable
fun <T> BaseScreen(
    uiState: T,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loadingMessage: String = "Loading...", // Allow customizing loading message
    content: @Composable (uiState: T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) { // Ensure BaseScreen fills its container
        when {
            isLoading -> LoadingScreen(
                modifier = Modifier.fillMaxSize(), // Ensure LoadingScreen also fills
                message = loadingMessage
            )
            error != null -> ErrorScreen(
                error = error,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize() // Ensure ErrorScreen also fills
            )
            else -> content(uiState)
        }
    }
}

/**
 * USAGE EXAMPLE for developers:
 *
 * This shows how a developer would use `BaseScreen` in their screen composable,
 * typically connecting it to a ViewModel that extends `BaseViewModel`.
 *
 * import androidx.compose.foundation.lazy.LazyColumn
 * import androidx.compose.foundation.lazy.items
 * import androidx.compose.material3.Text
 * import androidx.compose.runtime.Composable
 * import androidx.compose.runtime.collectAsState
 * import androidx.compose.runtime.getValue
 * import androidx.hilt.navigation.compose.hiltViewModel
 * import com.tomatomediacenter.presentation.viewmodels.HomeViewModel // Assuming it exists
 * import com.tomatomediacenter.presentation.viewmodels.HomeUiState // Assuming it exists
 * import com.tomatomediacenter.presentation.viewmodels.HomeEvent // Assuming it exists
 *
 * // Placeholder for Movie data class
 * // data class Movie(val id: String, val title: String)
 *
 * // Placeholder for MovieItem composable
 * // @Composable
 * // fun MovieItem(movie: Movie, modifier: Modifier = Modifier) {
 * //     Text(text = movie.title, modifier = modifier.padding(8.dp))
 * // }
 *
 * @Composable
 * fun HomeScreen(
 *     // viewModel: HomeViewModel = hiltViewModel() // HiltViewModel injects the ViewModel
 * ) {
 *     // val uiState by viewModel.uiState.collectAsState()
 *     // val isLoading by viewModel.isLoading.collectAsState()
 *     // val error by viewModel.error.collectAsState()
 *
 *     // Dummy data for example if ViewModel is not set up yet
 *     val isLoading = false
 *     val error: String? = null // or "Sample error for testing"
 *     // val uiState = HomeUiState(movies = listOf(Movie("1", "Sample Movie 1"), Movie("2", "Sample Movie 2")))
 *
 *     // BaseScreen(
 *     //     uiState = uiState,
 *     //     isLoading = isLoading,
 *     //     error = error,
 *     //     onRetry = { viewModel.onEvent(HomeEvent.LoadMovies) }, // Example retry event
 *     //     modifier = Modifier.fillMaxSize()
 *     // ) { state ->
 *     //     // Your actual UI content here, using 'state' (which is uiState)
 *     //     if (state.movies.isEmpty() && !isLoading) { // Handle empty state
 *     //         Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
 *     //             Text("No movies found.")
 *     //         }
 *     //     } else {
 *     //         LazyColumn(modifier = Modifier.fillMaxSize()) {
 *     //             items(state.movies) { movie ->
 *     //                 MovieItem(movie = movie)
 *     //             }
 *     //         }
 *     //     }
 *     // }
 * }
 */

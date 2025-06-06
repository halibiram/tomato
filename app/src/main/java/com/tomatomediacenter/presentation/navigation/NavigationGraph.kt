package com.tomatomediacenter.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
// import androidx.hilt.navigation.compose.hiltViewModel // For hiltViewModel()
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tomatomediacenter.presentation.ui.home.HomeScreen // Added import
// Placeholders for Screen Composables - these would be in their respective ui packages
// import com.tomatomediacenter.presentation.ui.search.SearchScreen
// import com.tomatomediacenter.presentation.ui.player.PlayerScreen
// import com.tomatomediacenter.presentation.ui.settings.SettingsScreen
// import com.tomatomediacenter.presentation.ui.downloads.DownloadsScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp


/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This file defines the core navigation structure for the application using Jetpack Compose
 *    Navigation. It centralizes route definitions (`Routes`, `NavArgs`) and the main navigation
 *    graph (`TomatoNavGraph`). This provides a clear overview of all navigable screens and
 *    how they are connected, making navigation management more straightforward.
 *
 * 2. How developers should use it:
 *    - `Routes`: Contains constant string values for all navigation routes. Developers use these
 *      constants when navigating (e.g., `navController.navigate(Routes.HOME)`).
 *    - `NavArgs`: Contains constant string keys for navigation arguments (e.g., `movieId`).
 *    - `TomatoNavGraph`: This is the main `NavHost` composable.
 *        - Each feature developer responsible for a screen will add a `composable` entry here
 *          for their screen.
 *        - Inside the `composable` block, they will call their screen's main Composable function.
 *        - Navigation actions (e.g., `onNavigateToSearch`, `onNavigateToPlayer`) are passed as
 *          lambdas to the screen composables, which then use the `navController` to perform
 *          the actual navigation.
 *
 * 3. What NOT to do:
 *    - Avoid hardcoding route strings directly in `navController.navigate()` calls. Always use
 *      the constants from the `Routes` object.
 *    - Do not make `navController` a global singleton or pass it unnecessarily deep into
 *      composable hierarchies. Pass navigation lambdas instead.
 *    - Avoid overly complex navigation logic directly within this file. For very complex
 *      nested graphs, consider breaking them into separate graph functions.
 *
 * 4. Common pitfalls to avoid:
 *    - Mismatched route strings or argument keys, leading to navigation errors or arguments
 *      not being received correctly.
 *    - Forgetting to handle `arguments` in `composable` definitions for routes that expect them.
 *    - Issues with backstack management if not using `popUpTo`, `launchSingleTop` appropriately.
 *    - Type safety of navigation arguments: While string-based by default, consider using
 *      libraries or patterns to achieve better type safety if routes become very complex.
 *
 * 5. Integration with other components:
 *    - `TomatoNavGraph` is typically hosted in the main `Activity` or a top-level app composable.
 *    - It uses a `NavHostController` (usually created with `rememberNavController()`) to manage
 *      navigation state.
 *    - Screen composables defined in their respective `presentation/ui/...` packages are called
 *      from within the `composable` blocks here.
 *    - ViewModels (e.g., using `hiltViewModel()`) are often instantiated within the scope of
 *      each `composable` route.
 *
 * 6. Testing strategies:
 *    - Test individual screen composables in isolation using `@Preview` and Compose UI tests.
 *    - For navigation logic, use the `androidx.navigation.testing` artifact to test
 *      `NavHostController` behavior and verify navigation between destinations.
 *    - Ensure that arguments are correctly passed and received by screens.
 */

/**
 * Defines unique string constants for all navigation routes in the application.
 * ALL DEVELOPERS MUST use these constants when defining navigation paths or navigating.
 */
object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val PLAYER = "player/{movieId}" // Example route with a mandatory argument
    const val SETTINGS = "settings"
    const val DOWNLOADS = "downloads"
    // Add other main routes here
    // Example: const val PROFILE = "profile"

    /**
     * Helper function to create a route to the Player screen with a specific movie ID.
     * @param movieId The ID of the movie to play.
     * @return The complete route string for the player screen.
     */
    fun playerWithMovieId(movieId: String): String {
        return "player/$movieId"
    }
}

/**
 * Defines constant keys for navigation arguments.
 * Used when defining routes and retrieving arguments in screen composables.
 */
object NavArgs {
    const val MOVIE_ID = "movieId" // Key for the movie ID argument in the PLAYER route
    // Add other argument keys here
    // Example: const val USER_ID = "userId"
}

/**
 * The main navigation graph for the Tomato Media Center application.
 * It sets up all the navigable destinations (screens) using Jetpack Compose Navigation.
 * Each developer responsible for a screen adds their screen's composable entry here.
 *
 * @param navController The NavHostController that manages this navigation graph.
 * @param startDestination The route for the initial screen to be displayed.
 * @param modifier Modifier for the NavHost.
 */
@Composable
fun TomatoNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.HOME, // Default start destination
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home Screen - Developer 15-16
        composable(Routes.HOME) {
            HomeScreen(
                // viewModel = hiltViewModel(), // Example of Hilt VM
                onNavigateToSearch = {
                    navController.navigate(Routes.SEARCH)
                },
                onNavigateToPlayer = { movieId ->
                    navController.navigate(Routes.playerWithMovieId(movieId))
                },
                onNavigateToDownloads = {
                    navController.navigate(Routes.DOWNLOADS)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        // Search Screen - Developer 17-18
        composable(Routes.SEARCH) {
            PlaceholderScreen(
                screenName = "Search Screen",
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { id ->
                    navController.navigate(Routes.playerWithMovieId(id))
                }
            )
            // SearchScreen(
            //     // viewModel = hiltViewModel(),
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     },
            //     onNavigateToPlayer = { movieId ->
            //         navController.navigate(Routes.playerWithMovieId(movieId))
            //     }
            // )
        }

        // Player Screen - Developer 9-10
        composable(
            route = Routes.PLAYER,
            // arguments = listOf(navArgument(NavArgs.MOVIE_ID) { type = NavType.StringType }) // If needed
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString(NavArgs.MOVIE_ID)
            PlaceholderScreen(
                screenName = "Player Screen (Movie ID: $movieId)",
                detailsId = movieId,
                onNavigateBack = { navController.popBackStack() }
            )
            // PlayerScreen(
            //     // viewModel = hiltViewModel(),
            //     movieId = movieId,
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }

        // Settings Screen - Developer 19-20
        composable(Routes.SETTINGS) {
            PlaceholderScreen(
                screenName = "Settings Screen",
                onNavigateBack = { navController.popBackStack() }
            )
            // SettingsScreen(
            //     // viewModel = hiltViewModel(),
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }

        // Downloads Screen - Developer 13-14
        composable(Routes.DOWNLOADS) {
            PlaceholderScreen(
                screenName = "Downloads Screen",
                onNavigateBack = { navController.popBackStack() }
            )
            // DownloadsScreen(
            //     // viewModel = hiltViewModel(),
            //     onNavigateBack = {
            //         navController.popBackStack()
            //     }
            // )
        }

        // Developers: Add more composable routes here for other screens/features
        // Example:
        // composable(Routes.PROFILE) {
        // ProfileScreen(
        // navController = navController, // Or pass specific navigation lambdas
        // // ... other parameters
        // )
        // }
    }
}

/**
 * Placeholder Composable for screens that are not yet implemented.
 * Helps in setting up the navigation graph before actual screen UIs are ready.
 */
@Composable
internal fun PlaceholderScreen(
    screenName: String,
    detailsId: String? = null,
    onNavigateToDetails: ((String) -> Unit)? = null,
    onNavigateToRoute: ((String) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Simple UI for placeholder
    // In a real app, these screens would be defined in their respective feature modules/packages
    // e.g., com.tomatomediacenter.presentation.ui.home.HomeScreen
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = screenName, style = MaterialTheme.typography.headlineMedium)
        detailsId?.let {
            Text("Details ID: $it", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Example navigation buttons
        if (screenName == "Home Screen") {
            Button(onClick = { onNavigateToDetails?.invoke("sampleMovie123") }) {
                Text("Go to Player (movie123)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigateToRoute?.invoke(Routes.SEARCH) }) {
                Text("Go to Search")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigateToRoute?.invoke(Routes.SETTINGS) }) {
                Text("Go to Settings")
            }
        }
        onNavigateBack?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = it) {
                Text("Go Back")
            }
        }
    }
}

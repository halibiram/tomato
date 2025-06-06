package com.tomatomediacenter.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This BaseViewModel class provides a standardized structure for all ViewModels in the application.
 *    It promotes a unidirectional data flow (UDF) pattern for managing UI state and handling events.
 *    It includes built-in support for managing loading states and error messages, which are common
 *    requirements for most screens.
 *
 * 2. How developers should use it:
 *    - Developers create specific ViewModel classes that extend `BaseViewModel<State, Event>`,
 *      where `State` is a data class representing the UI state for a specific screen, and `Event`
 *      is a sealed class/interface representing possible user actions or events for that screen.
 *    - Implement `createInitialState(): State` to define the starting state of the screen.
 *    - Implement `handleEvent(event: Event)` to process incoming events. This method is where
 *      use cases are typically called and state updates are triggered.
 *    - UI (e.g., Composables) observes `uiState: StateFlow<State>`, `isLoading: StateFlow<Boolean>`,
 *      and `error: StateFlow<String?>` to react to changes.
 *    - UI dispatches events to the ViewModel using the `onEvent(event: Event)` method.
 *
 * 3. What NOT to do:
 *    - Do not expose `MutableStateFlow` instances directly to the UI. Always expose `StateFlow`
 *      for read-only access to state.
 *    - Avoid complex logic in the `onEvent` method itself. Delegate actual event processing
 *      to `handleEvent` and keep `onEvent` primarily for managing loading/error states around
 *      the `handleEvent` call.
 *    - Do not directly reference Android framework classes like `Context` or `View`s in ViewModels
 *      if it can be avoided (use dependency injection for necessary Android services like
 *      ApplicationContext if needed, but prefer passing data).
 *
 * 4. Common pitfalls to avoid:
 *    - Forgetting to set `_isLoading.value = false` in a `finally` block or after all paths
 *      within `handleEvent` if not using the `onEvent` wrapper's automatic handling.
 *    - Creating overly large `State` data classes. Consider breaking down complex screens
 *      into smaller, manageable state objects or multiple ViewModels if necessary.
 *    - Race conditions or unintended consequences if multiple events modify the same piece
 *      of state concurrently without proper handling (though `MutableStateFlow` updates are atomic).
 *
 * 5. Integration with other components:
 *    - `BaseViewModel` instances are typically provided to UI components (Composables) using
 *      Hilt's `@HiltViewModel`.
 *    - They invoke `BaseUseCase` implementations to perform business logic.
 *    - The `State` and `Event` types are specific to the presentation layer.
 *    - They interact with navigation components to trigger screen transitions.
 *
 * 6. Testing strategies:
 *    - Use `kotlinx-coroutines-test` for testing suspend functions and Flows.
 *    - Mock `BaseUseCase` dependencies to isolate ViewModel logic.
 *    - Verify that `uiState`, `isLoading`, and `error` Flows emit the correct values in
 *      response to events and use case results.
 *    - Test the `createInitialState()` method.
 *    - Test each branch within `handleEvent(event: Event)`.
 */
abstract class BaseViewModel<State, Event> : ViewModel() {

    // Internal mutable state for the specific UI
    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    // Common loading state - managed by onEvent
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Common error handling - managed by onEvent
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Developers must implement this to provide the initial UI state.
     * Called when the ViewModel is initialized.
     */
    abstract fun createInitialState(): State

    /**
     * Developers must implement this to define how events are processed.
     * This is where business logic (use cases) should be called and state updated.
     * This method is wrapped by `onEvent` which handles loading and error states.
     */
    abstract fun handleEvent(event: Event)

    /**
     * Public method for the UI to dispatch events to the ViewModel.
     * This method manages the generic loading and error states around the
     * execution of `handleEvent`.
     */
    fun onEvent(event: Event) {
        viewModelScope.launch {
            // Reset error before handling new event
            if (_error.value != null) {
                _error.value = null
            }
            _isLoading.value = true
            try {
                handleEvent(event)
            } catch (e: Exception) {
                // Set error if handleEvent throws an unhandled exception
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Helper function for updating the UI state.
     * It takes a reducer function that transforms the current state to a new state.
     *
     * @param reducer A function that takes the current State and returns a new State.
     */
    protected fun updateState(reducer: (currentState: State) -> State) {
        _uiState.update { reducer(it) }
    }

    /**
     * Protected helper to explicitly set loading state if needed outside the main `onEvent` flow.
     * Prefer relying on the automatic `onEvent` loading management.
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Protected helper to explicitly set an error message if needed outside the main `onEvent` flow
     * or for errors not thrown as exceptions.
     * Prefer relying on the automatic `onEvent` error management.
     */
    protected fun setError(message: String?) {
        _error.value = message
    }
}

/**
 * USAGE EXAMPLE - For Home Screen Developer
 *
 * This example demonstrates how a developer would implement a ViewModel for a Home screen.
 *
 * Assumptions:
 * - `Movie` is a data class.
 * - `GetPopularMoviesUseCase` is an implementation of `BaseUseCaseNoParams<List<Movie>>`.
 * - `@HiltViewModel` and `@Inject` are used for Hilt dependency injection.
 *
 * import androidx.lifecycle.viewModelScope // Already imported
 * import dagger.hilt.android.lifecycle.HiltViewModel // For Hilt
 * import kotlinx.coroutines.flow.collect // Already imported (implicitly via asStateFlow)
 * import kotlinx.coroutines.launch // Already imported
 * import javax.inject.Inject // For Hilt
 *
 * // 1. Define State data class
 * data class HomeUiState(
 *     val movies: List<Movie> = emptyList(),
 *     val isRefreshing: Boolean = false // Example of specific loading state
 * )
 *
 * // 2. Define Events sealed class/interface
 * sealed class HomeEvent {
 *     object LoadMovies : HomeEvent()
 *     object RefreshMovies : HomeEvent()
 *     data class MovieClicked(val movieId: String) : HomeEvent() // Pass relevant data
 * }
 *
 * // Placeholder for Movie data class
 * // data class Movie(val id: String, val title: String)
 *
 * // Placeholder for GetPopularMoviesUseCase
 * // class GetPopularMoviesUseCase @Inject constructor() : BaseUseCaseNoParams<List<Movie>>() {
 * //     override suspend fun execute(parameters: Unit): Result<List<Movie>> {
 * //         // Mock implementation
 * //         kotlinx.coroutines.delay(1000) // Simulate network delay
 * //         return Result.success(listOf(Movie("1", "Movie 1"), Movie("2", "Movie 2")))
 * //         // return Result.failure(Exception("Failed to load movies")) // Simulate error
 * //     }
 * // }
 *
 * @HiltViewModel
 * class HomeViewModel @Inject constructor(
 *     private val getPopularMoviesUseCase: GetPopularMoviesUseCase
 * ) : BaseViewModel<HomeUiState, HomeEvent>() {
 *
 *     override fun createInitialState(): HomeUiState = HomeUiState()
 *
 *     override fun handleEvent(event: HomeEvent) {
 *         when (event) {
 *             is HomeEvent.LoadMovies -> loadMovies(forceRefresh = false)
 *             is HomeEvent.RefreshMovies -> {
 *                 updateState { it.copy(isRefreshing = true) } // Show specific refresh indicator
 *                 loadMovies(forceRefresh = true)
 *             }
 *             is HomeEvent.MovieClicked -> {
 *                 // Handle navigation or other actions, e.g.:
 *                 // _navigationEvent.value = NavigationCommand.ToMovieDetails(event.movieId)
 *                 println("Movie clicked: ${event.movieId}")
 *             }
 *         }
 *     }
 *
 *     private fun loadMovies(forceRefresh: Boolean = false) {
 *         // The generic isLoading is already true thanks to onEvent.
 *         // setError and setLoading are also available if more granular control is needed from here.
 *         viewModelScope.launch {
 *             getPopularMoviesUseCase().fold(
 *                 onSuccess = { movies ->
 *                     updateState { currentState ->
 *                         currentState.copy(movies = movies, isRefreshing = false)
 *                     }
 *                 },
 *                 onFailure = { exception ->
 *                     // The generic error is set by onEvent if an exception bubbles up.
 *                     // If getPopularMoviesUseCase returns Result.failure, that exception
 *                     // is caught by BaseUseCase's invoke and turned into Result.failure.
 *                     // Here, we're using fold, so we get the exception directly.
 *                     // We can set a custom error message or rely on the generic one.
 *                     setError(exception.message ?: "Failed to load movies")
 *                     updateState { it.copy(isRefreshing = false) } // Ensure refresh indicator is turned off
 *                 }
 *             )
 *             // If getPopularMoviesUseCase().collect { result -> ... } was used:
 *             // result.fold(...)
 *         }
 *     }
 * }
 */

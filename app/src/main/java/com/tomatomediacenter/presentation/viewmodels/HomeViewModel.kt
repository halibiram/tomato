package com.tomatomediacenter.presentation.viewmodels

import androidx.lifecycle.viewModelScope
import com.tomatomediacenter.domain.usecase.BaseUseCaseNoParams // Assuming this exists
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// Placeholder for Movie data class
data class Movie(val id: String, val title: String)

// Placeholder for GetPopularMoviesUseCase
// This would typically be in the domain layer
class GetPopularMoviesUseCase @Inject constructor() : BaseUseCaseNoParams<List<Movie>>() {
    override suspend fun execute(parameters: Unit): Result<List<Movie>> {
        kotlinx.coroutines.delay(1000) // Simulate network delay
        // Simulate success
        return Result.success(listOf(Movie("1", "Avengers: Endgame"), Movie("2", "The Lion King")))
        // Simulate error:
        // return Result.failure(Exception("Failed to load movies"))
    }
}


// 1. Define State data class
data class HomeUiState(
    val movies: List<Movie> = emptyList(),
    val isRefreshing: Boolean = false
)

// 2. Define Events sealed class/interface
sealed class HomeEvent {
    object LoadMovies : HomeEvent()
    object RefreshMovies : HomeEvent()
    data class MovieClicked(val movieId: String) : HomeEvent()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPopularMoviesUseCase: GetPopularMoviesUseCase
) : BaseViewModel<HomeUiState, HomeEvent>() {

    override fun createInitialState(): HomeUiState = HomeUiState()

    override fun handleEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadMovies -> loadMovies(forceRefresh = false)
            is HomeEvent.RefreshMovies -> {
                updateState { it.copy(isRefreshing = true) }
                loadMovies(forceRefresh = true)
            }
            is HomeEvent.MovieClicked -> {
                // Handle navigation or other actions here
                // For example, using a SharedFlow in ViewModel to send navigation events to UI
                println("Movie clicked: ${'$'}{event.movieId}")
            }
        }
    }

    private fun loadMovies(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // The generic isLoading is handled by BaseViewModel's onEvent
            getPopularMoviesUseCase(Unit).fold( // Make sure to call use case with Unit if it's BaseUseCaseNoParams
                onSuccess = { movies ->
                    updateState { currentState ->
                        currentState.copy(movies = movies, isRefreshing = false)
                    }
                },
                onFailure = { exception ->
                    // The generic error is set by BaseViewModel's onEvent if an exception bubbles up from handleEvent
                    // If the use case itself returns Result.failure, we might want to set error specifically
                    setError(exception.message ?: "Failed to load movies")
                    updateState { it.copy(isRefreshing = false) }
                }
            )
        }
    }

    // Call loadMovies when the ViewModel is initialized
    init {
        onEvent(HomeEvent.LoadMovies)
    }
}

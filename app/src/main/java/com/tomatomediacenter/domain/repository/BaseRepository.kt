package com.tomatomediacenter.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This BaseRepository class provides a standardized way to handle data operations involving
 *    a cache and a network source. It ensures that data is fetched and managed consistently
 *    across different features of the application. It promotes a single source of truth
 *    by coordinating between the local cache and remote data.
 *
 * 2. How developers should use it:
 *    - Developers should create feature-specific repositories (e.g., MovieRepository) that
 *      extend this BaseRepository.
 *    - They need to provide concrete implementations for the abstract functions:
 *      - `fetchFromNetwork()`: To get data from a remote API.
 *      - `getCachedData()`: To retrieve data from a local cache (e.g., Room database).
 *      - `cacheData(data: T)`: To save fetched data into the local cache.
 *    - The `getData(forceRefresh: Boolean)` function should be used by UseCases to request data.
 *      It handles the logic of serving cached data first and then fetching from the network.
 *
 * 3. What NOT to do:
 *    - Do not directly call `fetchFromNetwork()` or `getCachedData()` from UseCases or ViewModels.
 *      Always use the `getData()` method.
 *    - Do not implement custom caching logic within `fetchFromNetwork()`. Caching is handled
 *      by the `cacheData()` method and the overall `getData()` flow.
 *    - Avoid modifying the core logic of `getData()` in subclasses.
 *
 * 4. Common pitfalls to avoid:
 *    - Forgetting to call `cacheData()` after a successful network fetch.
 *    - Incorrectly implementing `getCachedData()` leading to stale or no data being emitted.
 *    - Issues with Flow emissions, like emitting multiple success/failure states improperly.
 *      The `getData()` flow is designed to handle this; ensure your overrides are compatible.
 *    - Not handling network or database exceptions properly within the overridden methods.
 *      These should be wrapped in `Result.failure()`.
 *
 * 5. Integration with other components:
 *    - `BaseRepository` is typically injected into `BaseUseCase` implementations.
 *    - It interacts with network data sources (e.g., Retrofit/Ktor services) and local
 *      data sources (e.g., Room DAOs).
 *    - The data (Type `T`) it handles is usually a domain model defined in `domain/models/`.
 *
 * 6. Testing strategies:
 *    - Use mock implementations of network services and DAOs to test the repository.
 *    - Verify that `fetchFromNetwork()` is called when `forceRefresh` is true or cache is empty.
 *    - Verify that `getCachedData()` is called and its data is emitted.
 *    - Verify that `cacheData()` is called with the correct data after a network fetch.
 *    - Test error handling paths (network failures, cache failures).
 */
abstract class BaseRepository<T> {

    /**
     * Network + Cache strategy
     * Developers only need to override these functions
     */
    abstract suspend fun fetchFromNetwork(): Result<T>
    abstract suspend fun getCachedData(): Flow<T?> // Emits null if no cache or cache is empty
    abstract suspend fun cacheData(data: T)

    /**
     * Standard data fetching pattern
     * All developers use the same approach
     *
     * @param forceRefresh If true, fetches fresh data from the network, bypassing cache for the initial emission.
     *                     Cache will still be updated.
     * @return A Flow emitting Result objects. It may first emit cached data, then network data.
     */
    suspend fun getData(forceRefresh: Boolean = false): Flow<Result<T>> = flow {
        try {
            // First emit cached data if not forcing refresh
            if (!forceRefresh) {
                getCachedData().collect { cached ->
                    cached?.let {
                        // Ensure we only emit if there's actual data
                        emit(Result.success(it))
                    }
                }
            }

            // Fetch fresh data from network
            val networkResult = fetchFromNetwork()

            if (networkResult.isSuccess) {
                val data = networkResult.getOrThrow()
                cacheData(data)
                emit(Result.success(data)) // Emit fresh data after caching
            } else {
                // If network failed, and we haven't emitted cached data yet (e.g., due to forceRefresh
                // or empty cache), emit the failure. If cached data was already emitted,
                // the UI might choose to ignore this network error or display a non-critical warning.
                emit(Result.failure(networkResult.exceptionOrNull() ?: Exception("Unknown error during network fetch")))
            }

        } catch (e: Exception) {
            // Catch any other exceptions during the flow execution
            emit(Result.failure(e))
        }
    }
}

/**
 * USAGE EXAMPLE - Show to developers
 *
 * This demonstrates how a developer would implement a specific repository, for example,
 * for fetching a list of movies.
 *
 * Assumptions for this example:
 * - `MovieApi` is an interface/class for network calls (e.g., using Retrofit/Ktor).
 * - `MovieDao` is a Data Access Object for Room database operations.
 * - `Movie` is a data class representing a movie entity.
 * - `@Inject` suggests usage with a dependency injection framework like Hilt.
 *
 * import kotlinx.coroutines.flow.Flow
 * import javax.inject.Inject // Assuming Hilt for DI
 *
 * // Placeholder for data class Movie
 * // data class Movie(val id: String, val title: String)
 *
 * // Placeholder for MovieApi interface
 * // interface MovieApi {
 * //     suspend fun getPopularMovies(): List<Movie> // Example API call
 * // }
 *
 * // Placeholder for MovieDao interface
 * // interface MovieDao {
 * //     fun getAllMovies(): Flow<List<Movie>?> // Example DAO call
 * //     suspend fun insertMovies(movies: List<Movie>) // Example DAO call
 * // }
 *
 * class MovieRepository @Inject constructor(
 *     private val movieApi: MovieApi,
 *     private val movieDao: MovieDao
 * ) : BaseRepository<List<Movie>>() {
 *
 *     override suspend fun fetchFromNetwork(): Result<List<Movie>> = try {
 *         val movies = movieApi.getPopularMovies()
 *         Result.success(movies)
 *     } catch (e: Exception) {
 *         Result.failure(e)
 *     }
 *
 *     override suspend fun getCachedData(): Flow<List<Movie>?> = movieDao.getAllMovies()
 *
 *     override suspend fun cacheData(data: List<Movie>) {
 *         movieDao.insertMovies(data)
 *     }
 * }
 */

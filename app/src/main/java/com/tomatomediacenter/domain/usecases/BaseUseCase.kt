package com.tomatomediacenter.domain.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This BaseUseCase class standardizes the execution of business logic operations.
 *    It ensures that all use cases (interactors) in the application follow a consistent
 *    pattern for receiving parameters, executing their specific logic, and returning results.
 *    It also abstracts away threading concerns by typically running operations on a background dispatcher.
 *
 * 2. How developers should use it:
 *    - Developers create specific use case classes that extend `BaseUseCase` (for use cases
 *      that take parameters) or `BaseUseCaseNoParams` (for use cases that don't).
 *    - The primary method to implement is `execute(parameters: P): Result<R>`, where `P` is the
 *      parameter type and `R` is the return type. This method contains the core business logic.
 *    - Use cases are invoked using the `invoke` operator, e.g., `getPopularMoviesUseCase()` or
 *      `getUserDetailsUseCase(userId)`.
 *    - The `Result<R>` wrapper should be used to indicate success or failure of the operation.
 *
 * 3. What NOT to do:
 *    - Do not put UI-related logic (e.g., showing Toasts, navigating screens) inside a use case.
 *      Use cases should focus solely on business logic.
 *    - Avoid direct interaction with Android framework classes (Context, Views, etc.) unless
 *      absolutely necessary and carefully considered (e.g., for certain utility use cases).
 *    - Do not make network calls or database queries directly from the use case. Instead,
 *      delegate these tasks to repositories (which extend `BaseRepository`).
 *
 * 4. Common pitfalls to avoid:
 *    - Overcomplicating the `execute` method. Keep it focused on a single responsibility.
 *    - Not properly handling exceptions within `execute` and returning a `Result.failure(e)`.
 *      Although the `invoke` method has a try-catch, specific error handling within `execute`
 *      can provide more context.
 *    - Blocking the main thread if the default `Dispatchers.IO` is overridden with a
 *      dispatcher that runs on the main thread (should be rare).
 *
 * 5. Integration with other components:
 *    - `BaseUseCase` instances are typically injected into ViewModels (`BaseViewModel`).
 *    - They orchestrate data flow by calling methods on one or more Repositories.
 *    - The parameters `P` and return type `R` are usually domain models.
 *
 * 6. Testing strategies:
 *    - Use mock repositories to test use cases in isolation.
 *    - Verify that the `execute` method calls the correct repository methods with the
 *      correct parameters.
 *    - Test both successful execution paths (returning `Result.success`) and failure paths
 *      (returning `Result.failure`).
 *    - Since `invoke` handles the `withContext` block, unit tests for `execute` can often
 *      be simpler, focusing on the logic itself.
 */
abstract class BaseUseCase<in P, R>(
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Main business logic goes here
     * Developers override this function
     */
    abstract suspend fun execute(parameters: P): Result<R>

    /**
     * Standard invoke pattern - all use cases called the same way
     * Executes the use case on the provided [coroutineDispatcher].
     */
    suspend operator fun invoke(parameters: P): Result<R> {
        return try {
            withContext(coroutineDispatcher) {
                execute(parameters)
            }
        } catch (exception: Exception) {
            // Catch any unexpected exceptions during context switching or execution
            Result.failure(exception)
        }
    }
}

/**
 * For use cases that do not require parameters.
 * This simplifies the call site, e.g., `getPopularMoviesUseCase()` instead of `getPopularMoviesUseCase(Unit)`.
 */
abstract class BaseUseCaseNoParams<R>(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseUseCase<Unit, R>(coroutineDispatcher) {

    /**
     * Overloads the invoke operator for parameterless execution.
     */
    suspend operator fun invoke(): Result<R> = invoke(Unit)
}

/**
 * USAGE EXAMPLE - Template for developers
 *
 * This example shows how to implement a use case for getting popular movies.
 * It doesn't take any parameters and relies on `MovieRepository`.
 *
 * Assumptions for this example:
 * - `MovieRepository` is an implementation of `BaseRepository<List<Movie>>`.
 * - `Movie` is a domain model.
 * - `@Inject` suggests usage with Hilt.
 * - `first()` is used on the Flow from `getData()` to get a single emission.
 *   Depending on the desired behavior (e.g., continuous updates vs. one-shot load),
 *   this might change (e.g., the Flow could be returned directly).
 *
 * import kotlinx.coroutines.flow.first
 * import javax.inject.Inject // Assuming Hilt for DI
 *
 * // Placeholder for data class Movie
 * // data class Movie(val id: String, val title: String)
 *
 * // Placeholder for MovieRepository
 * // abstract class MovieRepository : BaseRepository<List<Movie>>() { /* ... */ }
 *
 * class GetPopularMoviesUseCase @Inject constructor(
 *     private val movieRepository: MovieRepository
 * ) : BaseUseCaseNoParams<List<Movie>>() {
 *
 *     override suspend fun execute(parameters: Unit): Result<List<Movie>> {
 *         return try {
 *             // Example: Fetch data and get the first emission from the Flow.
 *             // The actual implementation might vary based on whether you want
 *             // to return a Flow or a single Result.
 *             val resultOfFlow = movieRepository.getData().first() // Gets the first Result<List<Movie>>
 *             resultOfFlow // This is already a Result<List<Movie>>, so just return it.
 *         } catch (e: Exception) {
 *             // This catch block might be redundant if movieRepository.getData().first()
 *             // itself returns a Result.failure or throws an exception caught by the
 *             // parent BaseUseCase's invoke(). However, specific error mapping can be done here.
 *             Result.failure(e)
 *         }
 *     }
 * }
 */

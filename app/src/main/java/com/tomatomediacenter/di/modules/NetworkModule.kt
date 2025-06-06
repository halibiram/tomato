package com.tomatomediacenter.di.modules

import android.content.Context // Required for BuildConfig, assuming it's in application module
import com.tomatomediacenter.BuildConfig // Assuming BuildConfig is accessible
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This Hilt module, `NetworkModule`, is responsible for providing all network-related
 *    dependencies for the application. This includes the Ktor `HttpClient` and any API
 *    service interfaces (like `MovieApi`). Centralizing network setup here makes it easy
 *    to configure, manage, and inject these dependencies wherever needed.
 *
 * 2. How developers should use it:
 *    - Developers can inject the `HttpClient` or specific API services (e.g., `MovieApi`)
 *      into their Repositories or other classes that require network access.
 *    - To add a new API service:
 *        1. Define the API interface (e.g., `UserApi`).
 *        2. Create an implementation for it (e.g., `UserApiImpl(httpClient)`).
 *        3. Add a `@Provides` function in this module to provide the `UserApi` instance,
 *           similar to `provideMovieApi`.
 *    - Configuration for the HttpClient (e.g., base URL, interceptors, serialization)
 *      is managed within this module.
 *
 * 3. What NOT to do:
 *    - Do not create `HttpClient` instances manually in Repositories or other classes.
 *      Always inject them using Hilt.
 *    - Avoid putting non-network-related dependencies in this module.
 *      Create separate modules for different concerns (e.g., `DatabaseModule`, `RepositoryModule`).
 *
 * 4. Common pitfalls to avoid:
 *    - Forgetting to annotate `@Provides` functions with `@Singleton` if the dependency
 *      should be a singleton, potentially leading to multiple instances of HttpClients.
 *    - Incorrectly configuring Ktor plugins (e.g., ContentNegotiation, Logging).
 *    - Issues with Proguard/R8 if serialization classes are not correctly kept, though
 *      Ktor's `kotlinx.serialization` is generally robust.
 *    - API key exposure: Ensure `BuildConfig.TMDB_API_KEY` is handled securely and
 *      not hardcoded directly in version control if this were a real production app
 *      (e.g., loaded from a secure properties file not checked into Git).
 *
 * 5. Integration with other components:
 *    - `HttpClient` and API services provided here are primarily injected into `data/remote`
 *      API implementations, which are then used by Repository implementations.
 *    - Depends on Ktor libraries for HTTP client functionality and serialization.
 *    - Uses `BuildConfig` to access sensitive information like API keys (ensure this
 *      is set up correctly in the `build.gradle` file).
 *
 * 6. Testing strategies:
 *    - For unit tests, Hilt modules can be replaced with test modules providing mock
 *      dependencies (e.g., a mock `MovieApi` or a `HttpClient` configured with a
 *      mock engine).
 *    - For integration tests, a real `HttpClient` can be used, possibly pointing to a
 *      test server or using Ktor's `MockEngine`.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides a singleton instance of the Ktor HttpClient.
     * Configured with:
     * - Android engine.
     * - JSON content negotiation (kotlinx.serialization).
     * - Logging for HTTP requests and responses (Level.INFO).
     * - Default request parameters: base URL for TMDB and API key.
     *
     * Note: `BuildConfig.TMDB_API_KEY` needs to be defined in your app's build.gradle file.
     * Example: `buildConfigField "String", "TMDB_API_KEY", ""your_api_key_here""`
     */
    @Provides
    @Singleton
    fun provideHttpClient(@ApplicationContext context: Context): HttpClient {
        // Accessing BuildConfig requires context in library modules,
        // but typically not in the app module if defined there.
        // Assuming TMDB_API_KEY is accessible via com.tomatomediacenter.BuildConfig
        val apiKey = try {
            com.tomatomediacenter.BuildConfig.TMDB_API_KEY
        } catch (e: Exception) {
            // Fallback or error if BuildConfig or API key is not found.
            // In a real app, this should be handled more gracefully,
            // perhaps by disabling features or logging a severe error.
            System.err.println("ERROR: TMDB_API_KEY not found in BuildConfig. Please ensure it's configured.")
            "FALLBACK_API_KEY_OR_EMPTY" // Provide a fallback or handle error
        }

        return HttpClient(Android) {
            // Configure JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Be lenient with unknown keys from API
                    coerceInputValues = true // Coerce incorrect types if possible (e.g. null to default)
                    prettyPrint = true       // For easier debugging of JSON in logs
                })
            }

            // Configure logging
            install(Logging) {
                logger = Logger.DEFAULT // You can provide a custom logger
                level = LogLevel.INFO   // Log HTTP headers and bodies
            }

            // Default request configuration
            defaultRequest {
                url("https://api.themoviedb.org/3/")
                parameter("api_key", apiKey)
                // Common headers can be added here, e.g.:
                // header("Authorization", "Bearer YOUR_TOKEN")
            }

            // Other plugins like HttpTimeout, HttpRequestRetry can be configured here
            // install(HttpTimeout) {
            //     requestTimeoutMillis = 15000
            //     connectTimeoutMillis = 15000
            //     socketTimeoutMillis = 15000
            // }
        }
    }

    /**
     * Provides a singleton instance of MovieApi.
     * This is a placeholder and assumes you have a `MovieApi` interface and
     * an implementation `MovieApiImpl(httpClient: HttpClient)`.
     *
     * Developers should replace this with their actual API services.
     */
    @Provides
    @Singleton
    fun provideMovieApi(httpClient: HttpClient): MovieApi {
        // return MovieApiImpl(httpClient)
        // Placeholder implementation until MovieApi and its Impl are created
        // This will need to be replaced with the actual implementation
        // For now, returning a simple object that adheres to a potential MovieApi interface
        return object : MovieApi {
            override suspend fun getPopularMovies(): List<Any> { // Replace Any with actual Movie type
                // Actual Ktor call would be here:
                // httpClient.get("movie/popular").body()
                println("MovieApi: getPopularMovies called (mock implementation)")
                return emptyList()
            }
        }
    }
}

/**
 * Placeholder for MovieApi interface.
 * Developers will define this based on their API requirements.
 * Example:
 * interface MovieApi {
 *   @GET("movie/popular")
 *   suspend fun getPopularMovies(@Query("page") page: Int): PopularMoviesResponse
 * }
 * For Ktor, it would look different:
 * interface MovieApi {
 *   suspend fun getPopularMovies(page: Int): PopularMoviesResponse
 * }
 * class MovieApiImpl(private val httpClient: HttpClient) : MovieApi {
 *   override suspend fun getPopularMovies(page: Int): PopularMoviesResponse {
 *     return httpClient.get("movie/popular") { parameter("page", page) }.body()
 *   }
 * }
 */
interface MovieApi {
    // Example function, replace with actual API methods
    suspend fun getPopularMovies(): List<Any> // Replace Any with your Movie model
}

/**
 * TEMPLATE for developers adding other API services:
 *
 * @Module
 * @InstallIn(SingletonComponent::class) // Or other appropriate component
 * object YourFeatureServiceModule { // Or add to existing NetworkModule
 *
 *     @Provides
 *     @Singleton
 *     fun provideYourApiService(httpClient: HttpClient): YourApiService {
 *         return YourApiServiceImpl(httpClient)
 *     }
 * }
 *
 * // Then define YourApiService interface and YourApiServiceImpl class
 * interface YourApiService {
 *     // suspend fun getSomeData(): YourData
 * }
 *
 * class YourApiServiceImpl(private val httpClient: HttpClient) : YourApiService {
 *     // override suspend fun getSomeData(): YourData {
 *     //     return httpClient.get("your/endpoint").body()
 *     // }
 * }
 */

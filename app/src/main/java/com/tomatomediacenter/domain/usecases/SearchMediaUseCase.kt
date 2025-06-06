package com.tomatomediacenter.domain.usecases

import com.tomatomediacenter.data.model.SearchResponse
import com.tomatomediacenter.data.remote.SearchApiService
// Potentially add imports for Result wrappers or other domain-level models if needed

/**
 * Use case for searching media items.
 * This class encapsulates the business logic for fetching search results
 * from the [SearchApiService].
 *
 * @property searchApiService The service responsible for making API calls.
 */
class SearchMediaUseCase(
    private val searchApiService: SearchApiService
) {

    /**
     * Executes the search media use case.
     *
     * @param query The search query string.
     * @param mediaType The media type to filter by (e.g., "movie", "tv", null for no filter).
     * @return A [SearchResponse] containing the search results, or null/error if the search failed.
     *         Consider using a Result wrapper for better error handling.
     */
    suspend fun execute(query: String, mediaType: String? = null): SearchResponse { // Or Flow<Resource<SearchResponse>>
        // More sophisticated logic can be added here:
        // - Caching
        // - Combining data from multiple sources
        // - Error handling and mapping
        // - If mediaType is used by the API, pass it to searchApiService.searchMovies
        //   This might require changing SearchApiService and its implementation.
        // For now, let's assume searchMovies in SearchApiService can handle a nullable mediaType.
        // If not, this use case would need to call different SearchApiService methods or
        // the SearchApiService itself would need to be updated.

        // Example: If SearchApiService's searchMovies method is updated to take mediaType
        // return searchApiService.searchMovies(query, mediaType)

        // For this example, let's assume the existing searchMovies only takes a query.
        // If mediaType is provided, and the API doesn't support filtering it directly on the
        // current endpoint, the use case *could* fetch all and then filter,
        // but this is inefficient and generally not recommended.
        // The ideal approach is to have the API support the filter.

        // For now, I will proceed as if searchApiService.searchMovies needs to be updated.
        // This change will be made in the next step if necessary.
        return searchApiService.searchMovies(query, mediaType)
    }
}

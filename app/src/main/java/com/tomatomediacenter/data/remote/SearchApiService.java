package com.tomatomediacenter.data.remote;

import com.tomatomediacenter.data.model.SearchResponse; // Assuming SearchResponse will be created here
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface defining the API endpoints for search functionality.
 */
public interface SearchApiService {

    /**
     * Searches for movies based on a query string.
     *
     * @param query The search query.
      * @param mediaType The type of media to search for (e.g., "movie", "tv"). Optional.
     * @return A {@link SearchResponse} object containing the search results.
     */
    @GET("search/multi") // Changed to "search/multi" as an example for filtered search
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("media_type") mediaType: String? // Assuming API parameter is "media_type"
    ): SearchResponse;
}

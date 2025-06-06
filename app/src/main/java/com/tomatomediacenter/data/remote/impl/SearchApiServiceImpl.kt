package com.tomatomediacenter.data.remote.impl;

import com.tomatomediacenter.data.remote.SearchApiService;
package com.tomatomediacenter.data.remote.impl

import com.tomatomediacenter.data.model.SearchResponse // Assuming SearchResponse will be created here
import com.tomatomediacenter.data.remote.SearchApiService
import retrofit2.Retrofit

/**
 * Implementation of the [SearchApiService] interface.
 * This class handles the actual network requests for search functionality.
 */
class SearchApiServiceImpl(retrofit: Retrofit) : SearchApiService {

    private val searchApiService: SearchApiService = retrofit.create(SearchApiService::class.java)

    /**
     * Searches for movies based on a query string.
     *
     * @param query The search query.
     * @return A [SearchResponse] object containing the search results.
     */
    override suspend fun searchMovies(query: String, mediaType: String?): SearchResponse {
        return searchApiService.searchMovies(query, mediaType)
    }
}

package com.tomatomediacenter.data.model

// Add necessary imports here (e.g., for serialization libraries like Gson or Moshi)

/**
 * Data class representing the response from the search API.
 *
 * @property results A list of [Movie] objects representing the search results.
 * @property totalResults The total number of results found.
 * @property page The current page number of the results.
 */
data class SearchResponse(
    val results: List<Movie>,
    val totalResults: Int,
    val page: Int
    // Add other relevant fields from the API response
)

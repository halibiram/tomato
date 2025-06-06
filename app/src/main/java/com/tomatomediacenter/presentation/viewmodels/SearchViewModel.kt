package com.tomatomediacenter.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomatomediacenter.data.model.SearchResponse
import com.tomatomediacenter.domain.usecases.SearchMediaUseCase
import kotlinx.coroutines.launch
// Import a Resource wrapper class if you have one for states like Loading, Success, Error

/**
 * ViewModel for the search functionality.
 * This class is responsible for fetching search results using the [SearchMediaUseCase]
 * and providing them to the UI (e.g., [SearchFragment]).
 *
 * @property searchMediaUseCase The use case for searching media.
 */
class SearchViewModel(
    private val searchMediaUseCase: SearchMediaUseCase
    // Add other dependencies if needed, e.g., Dispatchers
) : ViewModel() {

    // LiveData to hold the search results
    private val _searchResults = MutableLiveData<SearchResponse>() // Consider wrapping with Resource for states
    val searchResults: LiveData<SearchResponse> get() = _searchResults

    // LiveData to hold the loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to hold error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // LiveData for selected filter
    private val _mediaTypeFilter = MutableLiveData<String?>() // e.g., "movie", "tv", null for all
    val mediaTypeFilter: LiveData<String?> get() = _mediaTypeFilter

    /**
     * Sets the media type filter.
     *
     * @param mediaType The media type to filter by (e.g., "movie", "tv").
     *                  Pass null to clear the filter.
     */
    fun setMediaTypeFilter(mediaType: String?) {
        _mediaTypeFilter.value = mediaType
        // Optionally, trigger a new search if a query already exists
        // currentQuery.value?.let { search(it) }
    }

    // Keep track of the current query to re-trigger search when filters change
    private val _currentQuery = MutableLiveData<String?>()

    /**
     * Performs a search using the provided query and current filter settings.
     * Updates LiveData objects for results, loading state, and errors.
     *
     * @param query The search query string.
     */
    fun search(query: String) {
        _currentQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = SearchResponse(emptyList(), 0, 0) // Clear results
            _error.value = "Search query cannot be empty."
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        _error.value = null // Clear previous errors

        viewModelScope.launch {
            try {
                // Pass query and filter to use case
                // This assumes SearchMediaUseCase will be updated to handle filters
                val response = searchMediaUseCase.execute(query, _mediaTypeFilter.value)
                _searchResults.value = response
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _searchResults.value = SearchResponse(emptyList(), 0, 0) // Clear results on error
            } finally {
                _isLoading.value = false
            }
        }
    }
}

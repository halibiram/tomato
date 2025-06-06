package com.tomatomediacenter.presentation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tomatomediacenter.data.model.Movie
import com.tomatomediacenter.data.model.SearchResponse
import com.tomatomediacenter.domain.usecases.SearchMediaUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchViewModelTest {

    // Rule for LiveData testing
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var searchMediaUseCase: SearchMediaUseCase

    private lateinit var searchViewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        searchViewModel = SearchViewModel(searchMediaUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search with blank query sets error and does not trigger use case`() = runTest {
        searchViewModel.search("")

        assertNotNull(searchViewModel.error.value)
        assertEquals("Search query cannot be empty.", searchViewModel.error.value)
        assertNull(searchViewModel.searchResults.value) // Should be null or empty
        verify(searchMediaUseCase, never()).execute(anyString(), anyOrNull())
    }

    @Test
    fun `search with valid query triggers use case and updates LiveData on success`() = runTest {
        val query = "Inception"
        val mockResponse = SearchResponse(listOf(Movie(1, "Inception", "", "", "", 0.0)), 1, 1)
        `when`(searchMediaUseCase.execute(query, null)).thenReturn(mockResponse)

        searchViewModel.search(query)
        advanceUntilIdle() // Ensure coroutines complete

        assertNull(searchViewModel.error.value)
        assertEquals(mockResponse, searchViewModel.searchResults.value)
        assertEquals(false, searchViewModel.isLoading.value) // Should be false after completion
    }

    @Test
    fun `search with filter triggers use case with filter and updates LiveData`() = runTest {
        val query = "Batman"
        val filter = "movie"
        val mockResponse = SearchResponse(listOf(Movie(1, "Batman Begins", "", "", "", 0.0)), 1, 1)
        `when`(searchMediaUseCase.execute(query, filter)).thenReturn(mockResponse)

        searchViewModel.setMediaTypeFilter(filter)
        searchViewModel.search(query)
        advanceUntilIdle()

        assertEquals(mockResponse, searchViewModel.searchResults.value)
        verify(searchMediaUseCase).execute(query, filter)
    }

    @Test
    fun `search results in error when use case throws exception`() = runTest {
        val query = "Unknown"
        val errorMessage = "Network error"
        `when`(searchMediaUseCase.execute(query, null)).thenThrow(RuntimeException(errorMessage))

        searchViewModel.search(query)
        advanceUntilIdle()

        assertEquals(false, searchViewModel.isLoading.value)
        assertEquals("Search failed: $errorMessage", searchViewModel.error.value)
        // Assuming searchResults LiveData is cleared or set to a default empty state on error
        assertEquals(SearchResponse(emptyList(), 0, 0), searchViewModel.searchResults.value)
    }

    @Test
    fun `setMediaTypeFilter updates filter LiveData`() {
        val filter = "tv"
        searchViewModel.setMediaTypeFilter(filter)
        assertEquals(filter, searchViewModel.mediaTypeFilter.value)
    }
}

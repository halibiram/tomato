package com.tomatomediacenter.domain.usecases

import com.tomatomediacenter.data.model.Movie
import com.tomatomediacenter.data.model.SearchResponse
import com.tomatomediacenter.data.remote.SearchApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchMediaUseCaseTest {

    @Mock
    private lateinit var searchApiService: SearchApiService

    private lateinit var searchMediaUseCase: SearchMediaUseCase

    @Before
    fun setUp() {
        searchMediaUseCase = SearchMediaUseCase(searchApiService)
    }

    @Test
    fun `execute calls SearchApiService searchMovies with query and no filter`() = runTest {
        val query = "Interstellar"
        val mockResponse = SearchResponse(listOf(Movie(1, "Interstellar", "", "", "", 0.0)), 1, 1)
        `when`(searchApiService.searchMovies(query, null)).thenReturn(mockResponse)

        val result = searchMediaUseCase.execute(query)

        assertEquals(mockResponse, result)
        verify(searchApiService).searchMovies(query, null)
    }

    @Test
    fun `execute calls SearchApiService searchMovies with query and filter`() = runTest {
        val query = "The Office"
        val mediaType = "tv"
        val mockResponse = SearchResponse(listOf(Movie(1, "The Office", "", "", "", 0.0)), 1, 1)
        `when`(searchApiService.searchMovies(query, mediaType)).thenReturn(mockResponse)

        val result = searchMediaUseCase.execute(query, mediaType)

        assertEquals(mockResponse, result)
        verify(searchApiService).searchMovies(query, mediaType)
    }

    @Test
    fun `execute propagates exceptions from SearchApiService`() = runTest {
        val query = "ErrorQuery"
        val errorMessage = "API Error"
        `when`(searchApiService.searchMovies(query, null)).thenThrow(RuntimeException(errorMessage))

        var exception: Exception? = null
        try {
            searchMediaUseCase.execute(query)
        } catch (e: RuntimeException) {
            exception = e
        }

        assertNotNull(exception)
        assertEquals(errorMessage, exception?.message)
    }
}

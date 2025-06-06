package com.tomatomediacenter.data.remote

import com.google.gson.Gson
import com.tomatomediacenter.data.model.Movie
import com.tomatomediacenter.data.model.SearchResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class) // Using MockitoJUnitRunner though not strictly needed if no mocks here
class SearchApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var searchApiService: SearchApiService
    private val gson = Gson()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        searchApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // Use the mock server's URL
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SearchApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `searchMovies constructs correct request and parses response`() = runTest {
        val query = "Dune"
        val mediaType = "movie"
        val mockApiResponse = SearchResponse(
            results = listOf(Movie(id = 1, title = "Dune", overview = "Sci-fi epic", posterPath = "/dune.jpg", releaseDate = "2021-10-21", voteAverage = 8.0)),
            totalResults = 1,
            page = 1
        )
        val jsonResponse = gson.toJson(mockApiResponse)
        mockWebServer.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val response = searchApiService.searchMovies(query, mediaType)

        // Check the response
        assertNotNull(response)
        assertEquals(1, response.results.size)
        assertEquals("Dune", response.results[0].title)

        // Check the request made to MockWebServer
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/search/multi?query=Dune&media_type=movie", recordedRequest.path)
        assertEquals("GET", recordedRequest.method)
    }

    @Test
    fun `searchMovies with null mediaType constructs correct request`() = runTest {
        val query = "Dune"
         val mockApiResponse = SearchResponse(
            results = listOf(Movie(id = 1, title = "Dune", overview = "Sci-fi epic", posterPath = "/dune.jpg", releaseDate = "2021-10-21", voteAverage = 8.0)),
            totalResults = 1,
            page = 1
        )
        val jsonResponse = gson.toJson(mockApiResponse)
        mockWebServer.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))


        searchApiService.searchMovies(query, null) // mediaType is null

        val recordedRequest = mockWebServer.takeRequest()
        // Retrofit by default omits query parameters with null values
        assertEquals("/search/multi?query=Dune", recordedRequest.path)
    }

    @Test
    fun `searchMovies handles API error response`() = runTest {
        val query = "ErrorCase"
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))

        var exceptionThrown = false
        try {
            searchApiService.searchMovies(query, null)
        } catch (e: Exception) {
            // Depending on Retrofit's error handling (e.g., if it throws HttpException for non-2xx)
            exceptionThrown = true
        }
        assertTrue("Exception should be thrown for API error",exceptionThrown)
    }
}

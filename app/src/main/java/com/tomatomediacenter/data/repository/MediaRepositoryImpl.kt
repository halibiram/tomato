package com.tomatomediacenter.data.repository

import com.tomatomediacenter.domain.model.*
import com.tomatomediacenter.domain.repository.MediaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor() : MediaRepository {

    private val dummyMovies = listOf(
        MediaItem("1", "Movie A: The Beginning", "poster_a.jpg", "backdrop_a.jpg", "movie", "2023-01-15", 7.5),
        MediaItem("2", "Movie B: The Sequel", "poster_b.jpg", "backdrop_b.jpg", "movie", "2023-05-20", 8.1),
        MediaItem("3", "Movie C: The Final Chapter", "poster_c.jpg", "backdrop_c.jpg", "movie", "2024-01-10", 7.9)
    )

    private val dummyTvShows = listOf(
        MediaItem("101", "TV Show X: Season 1", "poster_x.jpg", "backdrop_x.jpg", "tv_show", "2022-09-01", 8.8),
        MediaItem("102", "TV Show Y: Limited Series", "poster_y.jpg", "backdrop_y.jpg", "tv_show", "2023-11-05", 9.2)
    )

    private val bookmarkedItems = MutableStateFlow<Set<String>>(emptySet())

    override fun getMediaByCategory(category: MediaCategory, page: Int): Flow<Result<List<MediaItem>>> = flow {
        delay(800) // Simulate network delay
        when (category) {
            MediaCategory.POPULAR_MOVIES, MediaCategory.TOP_RATED_MOVIES, MediaCategory.NOW_PLAYING_MOVIES -> {
                emit(Result.success(dummyMovies.shuffled().take(2)))
            }
            MediaCategory.POPULAR_TV_SHOWS, MediaCategory.TOP_RATED_TV_SHOWS -> {
                emit(Result.success(dummyTvShows.shuffled().take(1)))
            }
            MediaCategory.TRENDING_ALL_DAY, MediaCategory.TRENDING_ALL_WEEK -> {
                 emit(Result.success((dummyMovies + dummyTvShows).shuffled().take(3)))
            }
            else -> {
                emit(Result.success(emptyList()))
            }
        }
    }

    override fun getMediaDetails(mediaId: String, type: String?): Flow<Result<MediaDetails>> = flow {
        delay(1000) // Simulate network delay
        val item = dummyMovies.find { it.id == mediaId } ?: dummyTvShows.find { it.id == mediaId }
        if (item != null) {
            val details = MediaDetails(
                id = item.id,
                title = item.title,
                overview = "This is a detailed overview for ${item.title}. It would contain plot summaries, themes, and other relevant information about the content.",
                posterUrl = item.posterUrl,
                backdropUrl = item.backdropUrl,
                releaseDate = item.releaseDate,
                runtime = if (item.mediaType == "movie") 120 else 45, // minutes
                rating = item.rating,
                genres = listOf(Genre(1, "Action"), Genre(2, "Adventure")),
                cast = listOf(
                    CastMember(10, "Actor One", "Main Character", "actor1.jpg"),
                    CastMember(11, "Actor Two", "Supporting Character", "actor2.jpg")
                ),
                crew = listOf(CrewMember(20, "Director Smith", "Director", "director.jpg")),
                recommendations = dummyMovies.filterNot { it.id == mediaId }.take(2),
                similar = (dummyMovies + dummyTvShows).filterNot { it.id == mediaId }.shuffled().take(2),
                videos = listOf(Video("v1", "Official Trailer", "trailer_key", "YouTube", "Trailer")),
                seasons = if (item.mediaType == "tv_show") listOf(
                    Season(30, 1, "Season 1", 10, "2022-09-01", "season1_poster.jpg"),
                    Season(31, 2, "Season 2", 8, "2023-10-01", "season2_poster.jpg")
                ) else null,
                homepageUrl = "http://example.com/${item.id}",
                status = "Released",
                tagline = "An epic adventure!",
                voteCount = 12345,
                originalLanguage = "en",
                productionCompanies = listOf(ProductionCompany(50, "Dummy Studios", "logo.png", "US")),
                productionCountries = listOf(ProductionCountry("US", "United States of America"))
            )
            emit(Result.success(details))
        } else {
            emit(Result.failure(Exception("Media with ID $mediaId not found")))
        }
    }

    override fun searchMedia(query: String, page: Int): Flow<Result<List<MediaItem>>> = flow {
        delay(700)
        if (query.equals("empty", ignoreCase = true)) {
            emit(Result.success(emptyList()))
        } else {
            val results = (dummyMovies + dummyTvShows).filter {
                it.title.contains(query, ignoreCase = true)
            }
            emit(Result.success(results))
        }
    }

    override fun getRecommendations(mediaId: String?, page: Int): Flow<Result<List<MediaItem>>> = flow {
        delay(600)
        // If mediaId is provided, dummy logic could try to return items of the same type
        val sourceList = if (mediaId != null && dummyTvShows.any { it.id == mediaId }) dummyTvShows else dummyMovies
        emit(Result.success(sourceList.shuffled().take(2)))
    }

    override fun getSimilarMedia(mediaId: String, page: Int): Flow<Result<List<MediaItem>>> = flow {
        delay(600)
        val sourceItem = dummyMovies.find { it.id == mediaId } ?: dummyTvShows.find { it.id == mediaId }
        if (sourceItem != null) {
            val similar = (if (sourceItem.mediaType == "movie") dummyMovies else dummyTvShows)
                .filterNot { it.id == mediaId }
            emit(Result.success(similar.shuffled().take(2)))
        } else {
            emit(Result.success(emptyList()))
        }
    }

    override fun getBookmarkedMedia(): Flow<Result<List<MediaItem>>> = flow {
        val currentBookmarks = bookmarkedItems.value
        val items = (dummyMovies + dummyTvShows).filter { it.id in currentBookmarks }
        emit(Result.success(items))
    }

    override suspend fun addBookmark(mediaId: String): Result<Unit> {
        delay(300)
        bookmarkedItems.value = bookmarkedItems.value + mediaId
        return Result.success(Unit)
    }

    override suspend fun removeBookmark(mediaId: String): Result<Unit> {
        delay(300)
        bookmarkedItems.value = bookmarkedItems.value - mediaId
        return Result.success(Unit)
    }

    override fun isBookmarked(mediaId: String): Flow<Boolean> = flow {
        // This should ideally observe bookmarkedItems StateFlow for reactive updates
        // For simplicity in this dummy, just emitting current state
        emit(mediaId in bookmarkedItems.value)
        // A more correct reactive version:
        // bookmarkedItems.collect { bookmarks -> emit(mediaId in bookmarks) }
    }
}

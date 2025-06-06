package com.tomatomediacenter.domain.repository

import com.tomatomediacenter.domain.model.MediaItem
import com.tomatomediacenter.domain.model.MediaCategory
import com.tomatomediacenter.domain.model.MediaDetails
import kotlinx.coroutines.flow.Flow

/**
 * Interface for accessing and managing media content.
 * This repository provides methods for fetching various types of media,
 * searching content, and retrieving details for specific media items.
 * It abstracts the data sources (remote API, local database) from the use cases.
 */
interface MediaRepository {

    /**
     * Fetches a list of media items for a given category (e.g., popular movies, trending TV shows).
     *
     * @param category The category of media to fetch.
     * @param page The page number for pagination (if applicable).
     * @return A Flow emitting a list of [MediaItem] objects.
     *         The Flow will emit new lists if the underlying data changes (e.g., due to cache updates).
     */
    fun getMediaByCategory(category: MediaCategory, page: Int = 1): Flow<Result<List<MediaItem>>>

    /**
     * Fetches detailed information for a specific media item.
     *
     * @param mediaId The unique identifier of the media item.
     * @param type The type of media (e.g., movie, tv_show), if necessary to differentiate.
     * @return A Flow emitting [MediaDetails] for the requested item.
     *         The Flow will emit new details if the underlying data changes.
     */
    fun getMediaDetails(mediaId: String, type: String? = null): Flow<Result<MediaDetails>>

    /**
     * Searches for media items based on a query string.
     *
     * @param query The search query.
     * @param page The page number for pagination.
     * @return A Flow emitting a list of [MediaItem] objects matching the search query.
     */
    fun searchMedia(query: String, page: Int = 1): Flow<Result<List<MediaItem>>>

    /**
     * Fetches a list of recommended media items, possibly based on user preferences or a specific item.
     *
     * @param mediaId If provided, recommendations might be specific to this item.
     * @param page The page number for pagination.
     * @return A Flow emitting a list of recommended [MediaItem] objects.
     */
    fun getRecommendations(mediaId: String? = null, page: Int = 1): Flow<Result<List<MediaItem>>>

    /**
     * Fetches a list of similar media items to a given media item.
     *
     * @param mediaId The ID of the media item to find similar content for.
     * @param page The page number for pagination.
     * @return A Flow emitting a list of similar [MediaItem] objects.
     */
    fun getSimilarMedia(mediaId: String, page: Int = 1): Flow<Result<List<MediaItem>>>

    /**
     * Fetches media items from the user's watchlist or bookmarks.
     *
     * @return A Flow emitting a list of bookmarked [MediaItem] objects.
     */
    fun getBookmarkedMedia(): Flow<Result<List<MediaItem>>>

    /**
     * Adds a media item to the user's watchlist or bookmarks.
     *
     * @param mediaId The ID of the media item to bookmark.
     * @return Result indicating success or failure.
     */
    suspend fun addBookmark(mediaId: String): Result<Unit>

    /**
     * Removes a media item from the user's watchlist or bookmarks.
     *
     * @param mediaId The ID of the media item to remove from bookmarks.
     * @return Result indicating success or failure.
     */
    suspend fun removeBookmark(mediaId: String): Result<Unit>

    /**
     * Checks if a specific media item is bookmarked by the user.
     *
     * @param mediaId The ID of the media item to check.
     * @return A Flow emitting true if bookmarked, false otherwise.
     */
    fun isBookmarked(mediaId: String): Flow<Boolean>
}

// Note: The actual MediaItem, MediaCategory, and MediaDetails classes would need to be defined
// in the domain.model package. For example:
// package com.tomatomediacenter.domain.model
// data class MediaItem(val id: String, val title: String, val imageUrl: String?, val type: String)
// data class MediaDetails(val id: String, val title: String, val description: String, ...)
// enum class MediaCategory { POPULAR_MOVIES, TRENDING_TV_SHOWS, ... }

package com.tomatomediacenter.domain.model

/**
 * Represents a generic media item in a list or grid.
 * This is a simplified version, often used for display purposes in collections.
 *
 * @property id Unique identifier for the media item.
 * @property title The title of the media item.
 * @property posterUrl URL to the poster image. Nullable if not available.
 * @property backdropUrl URL to the backdrop image. Nullable if not available.
 * @property mediaType Type of media (e.g., "movie", "tv_show", "anime").
 * @property releaseDate Release date of the media item, if applicable.
 * @property rating Average user rating or score (e.g., 7.5). Nullable.
 */
data class MediaItem(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val mediaType: String, // Could be an enum: enum class MediaType { MOVIE, TV_SHOW, ANIME, OTHER }
    val releaseDate: String?, // Consider using a Date type or formatted string
    val rating: Double?
)

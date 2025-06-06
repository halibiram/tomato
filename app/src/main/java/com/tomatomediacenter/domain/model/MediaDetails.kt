package com.tomatomediacenter.domain.model

/**
 * Represents detailed information about a specific media item (movie, TV show, etc.).
 * This model is typically used when a user views the details screen of a media item.
 *
 * @property id Unique identifier for the media item.
 * @property title The title of the media item.
 * @property overview A synopsis or description of the media item.
 * @property posterUrl URL to the poster image.
 * @property backdropUrl URL to the backdrop image.
 * @property releaseDate Release date of the media item.
 * @property runtime Duration of the media item (e.g., in minutes for movies, or per episode for TV shows).
 * @property rating Average user rating or score.
 * @property genres List of genres associated with the media item.
 * @property cast List of main cast members.
 * @property crew List of main crew members (directors, writers).
 * @property recommendations List of recommended similar media items.
 * @property similar List of similar media items.
 * @property videos List of associated videos (trailers, teasers).
 * @property seasons List of seasons (for TV shows).
 * @property homepageUrl URL to the official homepage of the media.
 * @property status Current status (e.g., "Released", "In Production", "Ended").
 * @property tagline A tagline or catchphrase for the media.
 * @property voteCount Number of votes contributing to the rating.
 * @property originalLanguage Original language of the media.
 * @property productionCompanies List of production companies.
 * @property productionCountries List of production countries.
 */
data class MediaDetails(
    val id: String,
    val title: String,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String?, // Consider Date type
    val runtime: Int?, // in minutes
    val rating: Double?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val crew: List<CrewMember>,
    val recommendations: List<MediaItem>, // Simplified, could be more detailed
    val similar: List<MediaItem>,         // Simplified, could be more detailed
    val videos: List<Video>,
    val seasons: List<Season>?, // Specific to TV Shows
    val homepageUrl: String?,
    val status: String?,
    val tagline: String?,
    val voteCount: Int?,
    val originalLanguage: String?,
    val productionCompanies: List<ProductionCompany>?,
    val productionCountries: List<ProductionCountry>?
)

data class Genre(
    val id: Int,
    val name: String
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profilePath: String?
)

data class CrewMember(
    val id: Int,
    val name: String,
    val job: String, // e.g., "Director", "Writer"
    val profilePath: String?
)

data class Video(
    val id: String,
    val name: String,
    val key: String, // YouTube key, for example
    val site: String, // e.g., "YouTube"
    val type: String // e.g., "Trailer", "Teaser"
)

data class Season(
    val id: Int,
    val seasonNumber: Int,
    val name: String?,
    val episodeCount: Int,
    val airDate: String?, // Consider Date type
    val posterPath: String?
)

data class ProductionCompany(
    val id: Int,
    val name: String,
    val logoPath: String?,
    val originCountry: String?
)

data class ProductionCountry(
    val iso31661: String, // e.g., "US"
    val name: String
)

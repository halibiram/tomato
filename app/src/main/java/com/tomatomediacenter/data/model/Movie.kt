package com.tomatomediacenter.data.model

// Add necessary imports here (e.g., for serialization libraries like Gson or Moshi)

/**
 * Data class representing a movie.
 *
 * @property id The unique ID of the movie.
 * @property title The title of the movie.
 * @property overview A brief overview or synopsis of the movie.
 * @property posterPath The path to the movie's poster image.
 * @property releaseDate The release date of the movie.
 * @property voteAverage The average vote score for the movie.
 */
data class Movie(
    val id: Long,
    val title: String,
    val overview: String,
    val posterPath: String?, // Nullable if not always present
    val releaseDate: String?, // Nullable if not always present
    val voteAverage: Double? // Nullable if not always present
    // Add other relevant fields for a movie
)

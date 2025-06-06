package com.tomatomediacenter.domain.model

/**
 * Represents the different categories of media that can be fetched or displayed.
 * Each category might correspond to a specific API endpoint or query.
 *
 * @property apiPath A string value that can be used for API calls (e.g., "popular", "top_rated").
 * @property displayName A user-friendly string for display in the UI.
 */
enum class MediaCategory(val apiPath: String, val displayName: String) {
    POPULAR_MOVIES("movie/popular", "Popular Movies"),
    TOP_RATED_MOVIES("movie/top_rated", "Top Rated Movies"),
    UPCOMING_MOVIES("movie/upcoming", "Upcoming Movies"),
    NOW_PLAYING_MOVIES("movie/now_playing", "Now Playing Movies"),

    POPULAR_TV_SHOWS("tv/popular", "Popular TV Shows"),
    TOP_RATED_TV_SHOWS("tv/top_rated", "Top Rated TV Shows"),
    ON_THE_AIR_TV_SHOWS("tv/on_the_air", "On The Air TV Shows"),
    AIRING_TODAY_TV_SHOWS("tv/airing_today", "Airing Today TV Shows"),

    TRENDING_ALL_DAY("trending/all/day", "Trending Today (All)"),
    TRENDING_ALL_WEEK("trending/all/week", "Trending This Week (All)"),
    TRENDING_MOVIES_DAY("trending/movie/day", "Trending Movies Today"),
    TRENDING_MOVIES_WEEK("trending/movie/week", "Trending Movies This Week"),
    TRENDING_TV_SHOWS_DAY("trending/tv/day", "Trending TV Shows Today"),
    TRENDING_TV_SHOWS_WEEK("trending/tv/week", "Trending TV Shows This Week");

    // You can add more categories as needed, for example:
    // GENRE_ACTION_MOVIES("discover/movie?with_genres=28", "Action Movies"),
    // GENRE_COMEDY_TV_SHOWS("discover/tv?with_genres=35", "Comedy TV Shows");

    companion object {
        fun fromApiPath(path: String): MediaCategory? {
            return entries.find { it.apiPath == path }
        }
    }
}

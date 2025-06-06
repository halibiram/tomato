package com.tomatomediacenter.data.local.dao

import androidx.room.Dao
// import androidx.room.Insert
// import androidx.room.OnConflictStrategy
// import androidx.room.Query
// import com.tomatomediacenter.data.local.model.MovieEntity // Assuming MovieEntity exists
// import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Movie entities.
 *
 * Define your database interactions for movies here.
 * Use suspend functions for one-shot operations and Flow for observable queries.
 */
@Dao
interface MovieDao {
    // Example (uncomment and adapt when MovieEntity is defined):
    // @Query("SELECT * FROM movies")
    // fun getAllMovies(): Flow<List<MovieEntity>>
    //
    // @Query("SELECT * FROM movies WHERE id = :movieId")
    // suspend fun getMovieById(movieId: String): MovieEntity?
    //
    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    // suspend fun insertMovies(movies: List<MovieEntity>)
    //
    // @Query("DELETE FROM movies")
    // suspend fun deleteAllMovies()
}

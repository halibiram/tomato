package com.tomatomediacenter.di.modules

import android.content.Context
import androidx.room.Room
import com.tomatomediacenter.data.local.AppDatabase
import com.tomatomediacenter.data.local.dao.DownloadDao
import com.tomatomediacenter.data.local.dao.ExtensionDao
import com.tomatomediacenter.data.local.dao.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AI Leader must document EVERYTHING:
 *
 * 1. Why this pattern exists:
 *    This Hilt module, `DatabaseModule`, is responsible for providing all database-related
 *    dependencies. This primarily includes the Room `AppDatabase` instance and the
 *    Data Access Objects (DAOs) for each entity. Centralizing database setup
 *    ensures consistent configuration and easy injection of database components.
 *
 * 2. How developers should use it:
 *    - Developers can inject DAOs (e.g., `MovieDao`, `DownloadDao`) into their
 *      Repositories or other classes that need to interact with the local database.
 *    - To add a new entity and its DAO:
 *        1. Define the Room `@Entity` data class.
 *        2. Create the `@Dao` interface for that entity.
 *        3. Add the new entity to the `entities` array in the `AppDatabase` class definition.
 *        4. Add an abstract fun in `AppDatabase` to return the new DAO.
 *        5. Add a `@Provides` function in this module to provide the new DAO instance,
 *           similar to `provideMovieDao`.
 *
 * 3. What NOT to do:
 *    - Do not create `AppDatabase` instances manually. Always inject it or its DAOs via Hilt.
 *    - Avoid putting non-database-related dependencies in this module.
 *    - Do not perform database operations directly on the main thread. Room supports
 *      `suspend` functions in DAOs for coroutine-based asynchronous operations.
 *
 * 4. Common pitfalls to avoid:
 *    - Forgetting to increment the database version and provide a migration strategy
 *      when changing the database schema, leading to runtime crashes for existing users.
 *      `fallbackToDestructiveMigration()` is used here for simplicity during early
 *      development but should be replaced with proper migrations in production.
 *    - Defining DAOs or Entities incorrectly (e.g., missing annotations).
 *    - Complex queries that are inefficient; use Room's query validation and EXPLAIN QUERY.
 *
 * 5. Integration with other components:
 *    - `AppDatabase` and DAOs provided here are primarily injected into `data/local`
 *      DAO implementations which are then used by Repository implementations.
 *    - Depends on AndroidX Room libraries.
 *    - The DAOs interact with `@Entity` classes defined typically in `data/local/model` or similar.
 *
 * 6. Testing strategies:
 *    - For unit tests, Hilt modules can be replaced with test modules providing an
 *      in-memory Room database instance (`Room.inMemoryDatabaseBuilder()`).
 *    - Test DAO methods to ensure they correctly insert, query, update, and delete data.
 *    - Test migrations if they are implemented.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides a singleton instance of the Room AppDatabase.
     *
     * @param context The application context.
     * @return The singleton AppDatabase instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext, // Ensure it's application context
            AppDatabase::class.java,
            "tomato_database"
        )
        .fallbackToDestructiveMigration() // Destroys and recreates database on schema change.
                                          // Replace with proper migration strategies for production.
        // .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Example for migrations
        // .setQueryCallback(QueryCallback { sqlQuery, bindArgs -> // For debugging
        //     println("SQL Query: $sqlQuery SQL Args: $bindArgs")
        // }, Executors.newSingleThreadExecutor())
        .build()
    }

    /**
     * Provides MovieDao.
     *
     * @param database The AppDatabase instance.
     * @return The MovieDao instance.
     */
    @Provides
    fun provideMovieDao(database: AppDatabase): MovieDao {
        return database.movieDao()
    }

    /**
     * Provides DownloadDao.
     * (Template for other DAOs)
     *
     * @param database The AppDatabase instance.
     * @return The DownloadDao instance.
     */
    @Provides
    fun provideDownloadDao(database: AppDatabase): DownloadDao {
        return database.downloadDao()
    }

    /**
     * Provides ExtensionDao.
     * (Template for other DAOs)
     *
     * @param database The AppDatabase instance.
     * @return The ExtensionDao instance.
     */
    @Provides
    fun provideExtensionDao(database: AppDatabase): ExtensionDao {
        return database.extensionDao()
    }
}

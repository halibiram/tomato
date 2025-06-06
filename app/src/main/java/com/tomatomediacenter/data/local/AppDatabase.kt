package com.tomatomediacenter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tomatomediacenter.data.local.dao.DownloadDao
import com.tomatomediacenter.data.local.dao.ExtensionDao
import com.tomatomediacenter.data.local.dao.MovieDao
// Import your entity classes here once they are defined
// Example: import com.tomatomediacenter.data.local.model.MovieEntity
// import com.tomatomediacenter.data.local.model.DownloadEntity
// import com.tomatomediacenter.data.local.model.ExtensionEntity

/**
 * The Room database for the application.
 *
 * Entities should be added to the `entities` array.
 * DAOs should have corresponding abstract methods here.
 * Version number must be incremented on schema changes, and migrations provided.
 */
@Database(
    entities = [
        // Add your @Entity classes here, e.g.:
        // MovieEntity::class,
        // DownloadEntity::class,
        // ExtensionEntity::class
    ],
    version = 1, // Start with version 1
    exportSchema = false // Set to true if you want to export schema to a folder
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun downloadDao(): DownloadDao
    abstract fun extensionDao(): ExtensionDao

    // companion object {
    //     @Volatile
    //     private var INSTANCE: AppDatabase? = null
    //
    //     fun getDatabase(context: Context): AppDatabase {
    //         return INSTANCE ?: synchronized(this) {
    //             val instance = Room.databaseBuilder(
    //                 context.applicationContext,
    //                 AppDatabase::class.java,
    //                 "tomato_database"
    //             )
    //             .fallbackToDestructiveMigration() // For early dev; implement proper migrations later
    //             .build()
    //             INSTANCE = instance
    //             instance
    //         }
    //     }
    // }
}

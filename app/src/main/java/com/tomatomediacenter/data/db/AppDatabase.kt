package com.tomatomediacenter.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tomatomediacenter.data.db.dao.PlaceholderDao
import com.tomatomediacenter.data.db.entity.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = true // It's good practice to export schema for migrations
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placeholderDao(): PlaceholderDao

    // You can add a companion object for singleton instantiation if not using DI,
    // but since DI is likely (due to the 'di' package), I'll omit it for now.
    // If DI is not used, a typical singleton pattern would be:
    /*
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Database file name
                )
                // Add migrations here if needed
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    */
}

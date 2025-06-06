package com.tomatomediacenter.di

import android.content.Context
import androidx.room.Room
import com.tomatomediacenter.data.db.AppDatabase
import com.tomatomediacenter.data.db.dao.PlaceholderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database" // Ensure this matches the name used if manually instantiating
        )
        // In a real app, you'd add .addMigrations(...) here if you have migrations
        // .fallbackToDestructiveMigration() // Or this, for development/testing if you don't want to write migrations yet
        .build()
    }

    @Provides
    @Singleton // DAOs are usually singletons within the scope of the database
    fun providePlaceholderDao(appDatabase: AppDatabase): PlaceholderDao {
        return appDatabase.placeholderDao()
    }
}

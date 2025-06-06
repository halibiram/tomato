package com.tomatomediacenter.di.modules

import com.tomatomediacenter.data.auth.AuthServiceImpl
import com.tomatomediacenter.data.repository.MediaRepositoryImpl
import com.tomatomediacenter.domain.auth.AuthService
import com.tomatomediacenter.domain.repository.MediaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 *
 * This module tells Hilt how to provide instances of repository interfaces
 * when they are injected as dependencies elsewhere in the application.
 * It uses @Binds for better performance as the implementations are already
 * suitable for constructor injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds [AuthServiceImpl] to [AuthService] interface.
     * Hilt will provide an instance of [AuthServiceImpl] when [AuthService] is requested.
     * The [AuthServiceImpl] itself is annotated with @Singleton, so only one instance
     * will be created and shared throughout the application.
     */
    @Binds
    @Singleton // Ensure it's scoped to Singleton, consistent with AuthServiceImpl
    abstract fun bindAuthService(
        authServiceImpl: AuthServiceImpl
    ): AuthService

    /**
     * Binds [MediaRepositoryImpl] to [MediaRepository] interface.
     * Hilt will provide an instance of [MediaRepositoryImpl] when [MediaRepository] is requested.
     * The [MediaRepositoryImpl] itself is annotated with @Singleton, ensuring a single instance.
     */
    @Binds
    @Singleton // Ensure it's scoped to Singleton, consistent with MediaRepositoryImpl
    abstract fun bindMediaRepository(
        mediaRepositoryImpl: MediaRepositoryImpl
    ): MediaRepository
}

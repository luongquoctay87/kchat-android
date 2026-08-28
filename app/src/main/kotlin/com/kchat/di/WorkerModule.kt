package com.kchat.di

import com.kchat.data.repository.LocalCacheCleanupScheduler
import com.kchat.work.WorkManagerLocalCacheCleanupScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {
    @Binds
    @Singleton
    abstract fun bindLocalCacheCleanupScheduler(
        impl: WorkManagerLocalCacheCleanupScheduler,
    ): LocalCacheCleanupScheduler
}

package com.r8.doitagain.di

import com.r8.doitagain.data.ReminderRepository
import com.r8.doitagain.data.RoomReminderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReminderRepository(repository: RoomReminderRepository): ReminderRepository
}

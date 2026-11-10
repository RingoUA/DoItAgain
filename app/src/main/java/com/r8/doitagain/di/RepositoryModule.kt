package com.r8.doitagain.di

import com.r8.doitagain.data.ReminderRepository
import com.r8.doitagain.data.RoomReminderRepository
import com.r8.doitagain.reminders.AndroidReminderAlarmScheduler
import com.r8.doitagain.reminders.ReminderAlarmScheduler
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

    @Binds
    @Singleton
    abstract fun bindReminderAlarmScheduler(scheduler: AndroidReminderAlarmScheduler): ReminderAlarmScheduler
}

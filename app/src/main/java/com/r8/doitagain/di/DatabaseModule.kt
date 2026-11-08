package com.r8.doitagain.di

import android.content.Context
import androidx.room.Room
import com.r8.doitagain.data.ReminderDao
import com.r8.doitagain.data.ReminderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideReminderDatabase(@ApplicationContext context: Context): ReminderDatabase =
        Room.databaseBuilder(context, ReminderDatabase::class.java, "reminders.db").build()

    @Provides
    fun provideReminderDao(database: ReminderDatabase): ReminderDao = database.reminderDao()
}

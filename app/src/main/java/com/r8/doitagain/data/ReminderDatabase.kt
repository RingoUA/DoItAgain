package com.r8.doitagain.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY id ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder): Long
}

@Database(entities = [Reminder::class], version = 1, exportSchema = true)
internal abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}

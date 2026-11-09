package com.r8.doitagain.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY id ASC")
    fun observeAll(): Flow<List<Reminder>>

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder): Int

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long): Int
}

@Database(entities = [Reminder::class], version = 2, exportSchema = true)
internal abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    internal companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE reminders ADD COLUMN hour INTEGER NOT NULL DEFAULT 9")
                    db.execSQL("ALTER TABLE reminders ADD COLUMN minute INTEGER NOT NULL DEFAULT 0")
                    db.execSQL(
                        "ALTER TABLE reminders ADD COLUMN days_of_week_mask INTEGER NOT NULL DEFAULT 127",
                    )
                }
            }
    }
}

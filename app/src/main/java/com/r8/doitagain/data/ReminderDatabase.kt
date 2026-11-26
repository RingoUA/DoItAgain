package com.r8.doitagain.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder): Int

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM reminder_sessions ORDER BY reminder_id ASC")
    fun observeSessions(): Flow<List<ReminderSession>>

    @Query("SELECT * FROM reminder_sessions WHERE reminder_id = :reminderId")
    suspend fun getSession(reminderId: Long): ReminderSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: ReminderSession)

    @Query("DELETE FROM reminder_sessions WHERE reminder_id = :reminderId")
    suspend fun deleteSession(reminderId: Long)
}

@Database(entities = [Reminder::class, ReminderSession::class], version = 4, exportSchema = true)
internal abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    internal companion object {
        val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE reminders ADD COLUMN vibration_enabled INTEGER NOT NULL DEFAULT 1",
                    )
                }
            }

        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "ALTER TABLE reminders ADD COLUMN repeat_interval_minutes INTEGER NOT NULL DEFAULT 5",
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS reminder_sessions (
                            reminder_id INTEGER NOT NULL,
                            next_notification_at_millis INTEGER NOT NULL,
                            PRIMARY KEY(reminder_id)
                        )
                        """.trimIndent(),
                    )
                }
            }

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

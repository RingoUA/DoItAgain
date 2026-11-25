package com.r8.doitagain.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
internal data class Reminder(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "hour")
    val hour: Int = DEFAULT_REMINDER_HOUR,
    @ColumnInfo(name = "minute")
    val minute: Int = 0,
    @ColumnInfo(name = "days_of_week_mask")
    val daysOfWeekMask: Int = ALL_DAYS_MASK,
    @ColumnInfo(name = "repeat_interval_minutes")
    val repeatIntervalMinutes: Int = DEFAULT_REPEAT_INTERVAL_MINUTES,
) {
    init {
        require(title.isNotBlank()) { "Reminder title cannot be blank" }
        require(hour in 0..23) { "Reminder hour must be between 0 and 23" }
        require(minute in 0..59) { "Reminder minute must be between 0 and 59" }
        require(daysOfWeekMask in 0..ALL_DAYS_MASK) { "Reminder weekday mask must use only seven bits" }
        require(repeatIntervalMinutes > 0) { "Reminder repeat interval must be positive" }
    }

    internal companion object {
        const val DEFAULT_REMINDER_HOUR = 9
        const val ALL_DAYS_MASK = 0b1111111
        const val DEFAULT_REPEAT_INTERVAL_MINUTES = 5
    }
}

@Entity(tableName = "reminder_sessions")
internal data class ReminderSession(
    @PrimaryKey
    @ColumnInfo(name = "reminder_id")
    val reminderId: Long,
    @ColumnInfo(name = "next_notification_at_millis")
    val nextNotificationAtMillis: Long,
) {
    init {
        require(reminderId > 0) { "Reminder session id must be positive" }
        require(nextNotificationAtMillis >= 0) { "Next notification time cannot be negative" }
    }
}

internal enum class ReminderWeekday(val bit: Int) {
    MONDAY(1 shl 0),
    TUESDAY(1 shl 1),
    WEDNESDAY(1 shl 2),
    THURSDAY(1 shl 3),
    FRIDAY(1 shl 4),
    SATURDAY(1 shl 5),
    SUNDAY(1 shl 6),
}

package com.r8.doitagain.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.r8.doitagain.data.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal interface ReminderAlarmScheduler {
    fun scheduleAll(reminders: List<Reminder>)

    fun schedule(reminder: Reminder)

    fun cancel(reminderId: Long)
}

@Singleton
internal class AndroidReminderAlarmScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : ReminderAlarmScheduler {
        private val alarmManager = context.getSystemService(AlarmManager::class.java)

        override fun scheduleAll(reminders: List<Reminder>) {
            reminders.forEach(::schedule)
        }

        override fun schedule(reminder: Reminder) {
            cancel(reminder.id)
            val triggerAtMillis =
                nextReminderTime(reminder, Instant.now(), ZoneId.systemDefault())
                    ?.toEpochMilli() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent(reminder),
            )
        }

        override fun cancel(reminderId: Long) {
            val pendingIntent = pendingIntent(reminderId) ?: return
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        private fun pendingIntent(reminder: Reminder): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                alarmIntent(reminder.id)
                    .putExtra(EXTRA_TITLE, reminder.title)
                    .putExtra(EXTRA_HOUR, reminder.hour)
                    .putExtra(EXTRA_MINUTE, reminder.minute)
                    .putExtra(EXTRA_DAYS_OF_WEEK_MASK, reminder.daysOfWeekMask),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun pendingIntent(reminderId: Long): PendingIntent? =
            PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                alarmIntent(reminderId),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun alarmIntent(reminderId: Long): Intent =
            Intent(context, ReminderAlarmReceiver::class.java)
                .setData("doitagain://reminder/$reminderId".toUri())

        private companion object {
            const val EXTRA_TITLE = "title"
            const val EXTRA_HOUR = "hour"
            const val EXTRA_MINUTE = "minute"
            const val EXTRA_DAYS_OF_WEEK_MASK = "daysOfWeekMask"
        }
    }

internal fun nextReminderTime(
    reminder: Reminder,
    now: Instant,
    zoneId: ZoneId,
): Instant? {
    if (reminder.daysOfWeekMask == 0) return null

    val today = now.atZone(zoneId).toLocalDate()
    for (daysFromToday in 0..7) {
        val date = today.plusDays(daysFromToday.toLong())
        val weekdayBit = 1 shl (date.dayOfWeek.value - 1)
        if (reminder.daysOfWeekMask and weekdayBit == 0) continue

        val candidate =
            LocalDateTime
                .of(date, LocalTime.of(reminder.hour, reminder.minute))
                .atZone(zoneId)
                .toInstant()
        if (candidate.isAfter(now)) return candidate
    }
    return null
}

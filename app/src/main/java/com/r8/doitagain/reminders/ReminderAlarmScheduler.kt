package com.r8.doitagain.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.r8.doitagain.data.Reminder
import com.r8.doitagain.data.ReminderSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

internal const val ACTION_SESSION_ALARM = "com.r8.doitagain.action.SESSION_ALARM"

@Singleton
internal interface ReminderAlarmScheduler {
    fun scheduleAll(reminders: List<Reminder>)

    fun schedule(reminder: Reminder)

    fun scheduleSession(session: ReminderSession)

    fun cancel(reminderId: Long)

    fun cancelSession(reminderId: Long)
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
            cancelReminderAlarm(reminder.id)
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

        override fun scheduleSession(session: ReminderSession) {
            cancelSession(session.reminderId)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                return
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                session.nextNotificationAtMillis,
                sessionPendingIntent(session.reminderId) ?: return,
            )
        }

        override fun cancel(reminderId: Long) {
            cancelReminderAlarm(reminderId)
            cancelSession(reminderId)
        }

        override fun cancelSession(reminderId: Long) {
            val pendingIntent = sessionPendingIntent(reminderId, create = false) ?: return
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        private fun cancelReminderAlarm(reminderId: Long) {
            val pendingIntent = pendingIntent(reminderId) ?: return
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }

        private fun pendingIntent(reminder: Reminder): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                alarmIntent(reminder.id),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun pendingIntent(reminderId: Long): PendingIntent? =
            PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                alarmIntent(reminderId),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )

        private fun sessionPendingIntent(
            reminderId: Long,
            create: Boolean = true,
        ): PendingIntent? =
            PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                sessionAlarmIntent(reminderId),
                (if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE) or
                    PendingIntent.FLAG_IMMUTABLE,
            )

        private fun alarmIntent(reminderId: Long): Intent =
            Intent(context, ReminderAlarmReceiver::class.java)
                .setData("doitagain://reminder/$reminderId".toUri())

        private fun sessionAlarmIntent(reminderId: Long): Intent =
            Intent(context, ReminderAlarmReceiver::class.java)
                .setAction(ACTION_SESSION_ALARM)
                .setData("doitagain://reminder/$reminderId/session".toUri())

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

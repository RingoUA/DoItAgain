package com.r8.doitagain.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.r8.doitagain.data.Reminder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmScheduler: ReminderAlarmScheduler

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val hour = intent.getIntExtra(EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1)
        val daysOfWeekMask = intent.getIntExtra(EXTRA_DAYS_OF_WEEK_MASK, -1)
        val reminderId = intent.data?.lastPathSegment?.toLongOrNull() ?: return
        if (hour !in 0..23 || minute !in 0..59 || daysOfWeekMask !in 0..Reminder.ALL_DAYS_MASK) return

        showReminderNotification(context, reminderId, title)
        alarmScheduler.schedule(
            Reminder(
                id = reminderId,
                title = title,
                hour = hour,
                minute = minute,
                daysOfWeekMask = daysOfWeekMask,
            ),
        )
    }

    private companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_HOUR = "hour"
        const val EXTRA_MINUTE = "minute"
        const val EXTRA_DAYS_OF_WEEK_MASK = "daysOfWeekMask"
    }
}

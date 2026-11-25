package com.r8.doitagain.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.r8.doitagain.data.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmScheduler: ReminderAlarmScheduler
    @Inject lateinit var reminderRepository: ReminderRepository

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val reminderId = intent.data?.pathSegments?.firstOrNull()?.toLongOrNull() ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_CONFIRM_REMINDER -> {
                        reminderRepository.deleteSession(reminderId)
                        alarmScheduler.cancelSession(reminderId)
                        NotificationManagerCompat.from(context).cancel(reminderId.hashCode())
                    }
                    ACTION_SNOOZE_REMINDER, ACTION_DISMISS_REMINDER -> {
                        val reminder = reminderRepository.getReminder(reminderId) ?: return@launch
                        val session = reminderRepository.getSession(reminderId) ?: return@launch
                        NotificationManagerCompat.from(context).cancel(reminderId.hashCode())
                        val updatedSession =
                            session.copy(
                                nextNotificationAtMillis =
                                    System.currentTimeMillis() +
                                        reminder.repeatIntervalMinutes * MILLIS_PER_MINUTE,
                            )
                        reminderRepository.saveSession(updatedSession)
                        alarmScheduler.scheduleSession(updatedSession)
                    }
                }
            } catch (error: Exception) {
                Log.e(TAG, "Could not process reminder action", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ReminderActionReceiver"
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

internal const val ACTION_CONFIRM_REMINDER = "com.r8.doitagain.action.CONFIRM_REMINDER"
internal const val ACTION_SNOOZE_REMINDER = "com.r8.doitagain.action.SNOOZE_REMINDER"
internal const val ACTION_DISMISS_REMINDER = "com.r8.doitagain.action.DISMISS_REMINDER"

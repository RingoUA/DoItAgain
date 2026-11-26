package com.r8.doitagain.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.r8.doitagain.data.ReminderRepository
import com.r8.doitagain.data.ReminderSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ReminderAlarmReceiver : BroadcastReceiver() {
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
                val reminder = reminderRepository.getReminder(reminderId) ?: return@launch
                if (intent.action == ACTION_SESSION_ALARM) {
                    val session = reminderRepository.getSession(reminderId) ?: return@launch
                    val now = System.currentTimeMillis()
                    if (session.nextNotificationAtMillis > now) {
                        alarmScheduler.scheduleSession(session)
                        return@launch
                    }
                    showReminderNotification(context, reminder.id, reminder.title, reminder.vibrationEnabled)
                    val updatedSession =
                        session.copy(
                            nextNotificationAtMillis =
                                now + reminder.repeatIntervalMinutes * MILLIS_PER_MINUTE,
                        )
                    reminderRepository.saveSession(updatedSession)
                    alarmScheduler.scheduleSession(updatedSession)
                } else {
                    val now = System.currentTimeMillis()
                    val existingSession = reminderRepository.getSession(reminderId)
                    val session: ReminderSession
                    if (existingSession == null) {
                        session =
                            ReminderSession(
                                reminderId = reminderId,
                                nextNotificationAtMillis =
                                    now + reminder.repeatIntervalMinutes * MILLIS_PER_MINUTE,
                            )
                        reminderRepository.saveSession(session)
                    } else {
                        session = existingSession
                    }
                    showReminderNotification(context, reminder.id, reminder.title, reminder.vibrationEnabled)
                    alarmScheduler.scheduleSession(session)
                    alarmScheduler.schedule(reminder)
                }
            } catch (error: Exception) {
                Log.e(TAG, "Could not process reminder alarm", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "ReminderAlarmReceiver"
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

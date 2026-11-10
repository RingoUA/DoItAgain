package com.r8.doitagain.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.r8.doitagain.data.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class ReminderRescheduleReceiver : BroadcastReceiver() {
    @Inject lateinit var alarmScheduler: ReminderAlarmScheduler
    @Inject lateinit var reminderRepository: ReminderRepository

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                alarmScheduler.scheduleAll(reminderRepository.reminders.first())
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val RESCHEDULE_ACTIONS =
            setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
            )
    }
}

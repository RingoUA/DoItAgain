package com.r8.doitagain.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.r8.doitagain.R

internal const val REMINDER_NOTIFICATION_CHANNEL_ID = "reminders"
private const val REMINDER_VIBRATION_CHANNEL_ID = "reminders_vibration"
private const val REMINDER_NO_VIBRATION_CHANNEL_ID = "reminders_no_vibration"

internal fun createReminderNotificationChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    val channels =
        listOf(
            NotificationChannel(
                REMINDER_VIBRATION_CHANNEL_ID,
                context.getString(R.string.reminder_notification_channel),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.reminder_notification_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            },
            NotificationChannel(
                REMINDER_NO_VIBRATION_CHANNEL_ID,
                context.getString(R.string.reminder_notification_channel),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.reminder_notification_channel_description)
                enableVibration(false)
            },
        )
    manager.createNotificationChannels(channels)
}

internal fun showReminderNotification(
    context: Context,
    reminderId: Long,
    title: String,
    vibrationEnabled: Boolean = true,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val channelId =
        if (vibrationEnabled) REMINDER_VIBRATION_CHANNEL_ID else REMINDER_NO_VIBRATION_CHANNEL_ID
    val notification =
        NotificationCompat
            .Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                if (vibrationEnabled) {
                    setVibrate(longArrayOf(0, 500, 200, 500))
                } else {
                    setVibrate(longArrayOf(0))
                }
            }
            .setDeleteIntent(
                actionPendingIntent(context, reminderId, ACTION_DISMISS_REMINDER),
            )
            .addAction(
                0,
                context.getString(R.string.reminder_notification_snooze),
                actionPendingIntent(context, reminderId, ACTION_SNOOZE_REMINDER),
            )
            .addAction(
                0,
                context.getString(R.string.reminder_notification_confirm),
                actionPendingIntent(context, reminderId, ACTION_CONFIRM_REMINDER),
            )
            .build()
    NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
}

private fun actionPendingIntent(
    context: Context,
    reminderId: Long,
    action: String,
): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        reminderId.hashCode(),
        Intent(context, ReminderActionReceiver::class.java)
            .setAction(action)
            .setData("doitagain://reminder/$reminderId/${action.substringAfterLast('.').lowercase()}".toUri()),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

package com.r8.doitagain.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.r8.doitagain.R

internal const val REMINDER_NOTIFICATION_CHANNEL_ID = "reminders"

internal fun createReminderNotificationChannel(context: Context) {
    val channel =
        NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.reminder_notification_channel),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.reminder_notification_channel_description)
        }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
}

internal fun showReminderNotification(
    context: Context,
    reminderId: Long,
    title: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val notification =
        NotificationCompat
            .Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
    NotificationManagerCompat.from(context).notify(reminderId.hashCode(), notification)
}

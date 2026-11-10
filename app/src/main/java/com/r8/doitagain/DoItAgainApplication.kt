package com.r8.doitagain

import android.app.Application
import com.r8.doitagain.reminders.createReminderNotificationChannel
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DoItAgainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createReminderNotificationChannel(this)
    }
}

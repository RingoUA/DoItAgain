package com.r8.doitagain.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal interface ReminderRepository {
    val reminders: Flow<List<Reminder>>

    suspend fun add(reminder: Reminder)
}

internal class RoomReminderRepository
    @Inject
    constructor(
        private val dao: ReminderDao,
    ) : ReminderRepository {
        override val reminders: Flow<List<Reminder>> = dao.observeAll()

        override suspend fun add(reminder: Reminder) {
            dao.insert(reminder.copy(title = reminder.title.trim()))
        }
    }

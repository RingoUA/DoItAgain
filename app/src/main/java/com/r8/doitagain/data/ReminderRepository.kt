package com.r8.doitagain.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal interface ReminderRepository {
    val reminders: Flow<List<Reminder>>

    suspend fun add(reminder: Reminder)

    suspend fun update(reminder: Reminder)

    suspend fun delete(id: Long)
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

        override suspend fun update(reminder: Reminder) {
            dao.update(reminder.copy(title = reminder.title.trim()))
        }

        override suspend fun delete(id: Long) {
            dao.delete(id)
        }
    }

package com.r8.doitagain.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal interface ReminderRepository {
    val reminders: Flow<List<Reminder>>

    val sessions: Flow<List<ReminderSession>>

    suspend fun getReminder(id: Long): Reminder?

    suspend fun getSession(reminderId: Long): ReminderSession?

    suspend fun saveSession(session: ReminderSession)

    suspend fun deleteSession(reminderId: Long)

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

        override val sessions: Flow<List<ReminderSession>> = dao.observeSessions()

        override suspend fun getReminder(id: Long): Reminder? = dao.getById(id)

        override suspend fun getSession(reminderId: Long): ReminderSession? = dao.getSession(reminderId)

        override suspend fun saveSession(session: ReminderSession) {
            dao.saveSession(session)
        }

        override suspend fun deleteSession(reminderId: Long) {
            dao.deleteSession(reminderId)
        }

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

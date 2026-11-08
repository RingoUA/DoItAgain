package com.r8.doitagain.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
class ReminderPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "reminders-test-${UUID.randomUUID()}.db"
    private var database: ReminderDatabase? = null

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun tasksSurviveClosingAndReopeningDatabase() =
        runBlocking {
            val firstDatabase = openDatabase()
            val repository = RoomReminderRepository(firstDatabase.reminderDao())
            assertTrue(withTimeout(5_000.milliseconds) { repository.reminders.first() }.isEmpty())
            repository.add(Reminder(title = "  Моя задача 💧  "))
            repository.add(Reminder(title = "Моя задача 💧"))
            val original = withTimeout(5_000.milliseconds) { repository.reminders.first() }
            assertEquals(listOf("Моя задача 💧", "Моя задача 💧"), original.map { it.title })
            assertNotEquals(original[0].id, original[1].id)
            firstDatabase.close()

            val reopened = RoomReminderRepository(openDatabase().reminderDao())
            assertEquals(original, withTimeout(5_000.milliseconds) { reopened.reminders.first() })
        }

    @Test
    fun repositoryRejectsBlankTitlesWithoutWritingRows() =
        runBlocking {
            val repository = RoomReminderRepository(openDatabase().reminderDao())
            val failure = runCatching { repository.add(Reminder(title = " \n\t ")) }.exceptionOrNull()
            assertTrue(failure is IllegalArgumentException)
            assertTrue(withTimeout(5_000.milliseconds) { repository.reminders.first() }.isEmpty())
        }

    @Test
    fun repositoryUpdatesAndDeletesTasks() =
        runBlocking {
            val repository = RoomReminderRepository(openDatabase().reminderDao())
            repository.add(Reminder(title = "Read"))
            val original = withTimeout(5_000.milliseconds) { repository.reminders.first() }.single()

            repository.update(original.copy(title = "  Read a book  "))
            assertEquals(
                listOf(original.copy(title = "Read a book")),
                withTimeout(5_000.milliseconds) { repository.reminders.first() },
            )

            repository.delete(original.id)
            assertTrue(withTimeout(5_000.milliseconds) { repository.reminders.first() }.isEmpty())
        }

    private fun openDatabase(): ReminderDatabase =
        Room
            .databaseBuilder(context, ReminderDatabase::class.java, databaseName)
            .build()
            .also { database = it }
}

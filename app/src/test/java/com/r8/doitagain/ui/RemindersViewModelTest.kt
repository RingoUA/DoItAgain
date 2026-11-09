package com.r8.doitagain.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.r8.doitagain.data.Reminder
import com.r8.doitagain.data.ReminderWeekday
import com.r8.doitagain.data.ReminderRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val viewModelStore = ViewModelStore()
    private val repository = FakeReminderRepository()
    private var modelCount = 0

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun blankTitleAndCancelDoNotCreateTasks() =
        runTest {
            val model = createModel()
            model.startAdding()
            model.changeTitle("   ")
            model.save()
            runCurrent()
            assertEquals(0, repository.addCalls)
            model.changeTitle("Do not save")
            model.cancelAdding()
            runCurrent()
            assertEquals(0, repository.addCalls)
            assertNull(model.state.value.editor)
        }

    @Test
    fun savingTrimsTitleAndIgnoresRepeatedClicksUntilCommit() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            repository.saveGate = gate
            val savedState = SavedStateHandle()
            val model = createModel(savedState)
            model.startAdding()
            model.changeTitle("  Моя задача 💧  ")
            model.save()
            model.save()
            runCurrent()
            assertEquals(1, repository.addCalls)
            assertEquals(
                SavingStatus.SAVING,
                model.state.value.editor
                    ?.status,
            )
            model.cancelAdding()
            model.changeTitle("Changed while saving")
            assertEquals(
                AddReminderState(title = "  Моя задача 💧  ", status = SavingStatus.SAVING),
                model.state.value.editor,
            )

            gate.complete(Unit)
            runCurrent()
            assertEquals(
                listOf("Моя задача 💧"),
                model.state.value.reminders
                    .map { it.title },
            )
            assertNull(model.state.value.editor)
            assertNull(createModel(copySavedState(savedState)).state.value.editor)
        }

    @Test
    fun failedSaveKeepsDraftAndAllowsRetry() =
        runTest {
            repository.saveFails = true
            val model = createModel()
            model.startAdding()
            model.changeTitle("Read")
            model.save()
            runCurrent()
            assertEquals(
                AddReminderState(title = "Read", status = SavingStatus.ERROR),
                model.state.value.editor,
            )

            repository.saveFails = false
            model.save()
            assertEquals(
                SavingStatus.SAVING,
                model.state.value.editor
                    ?.status,
            )
            runCurrent()
            assertNull(model.state.value.editor)
            assertEquals(1, model.state.value.reminders.size)
        }

    @Test
    fun restoredDraftDoesNotCreateTaskUntilSaved() =
        runTest {
            val savedState = SavedStateHandle()
            val original = createModel(savedState)
            original.startAdding()
            original.changeTitle("Draft")
            original.changeTime(18, 45)
            original.toggleWeekday(ReminderWeekday.SUNDAY.bit)
            val restored = createModel(copySavedState(savedState))
            runCurrent()
            assertEquals(
                AddReminderState(
                    title = "Draft",
                    hour = 18,
                    minute = 45,
                    daysOfWeekMask = Reminder.ALL_DAYS_MASK xor ReminderWeekday.SUNDAY.bit,
                ),
                restored.state.value.editor,
            )
            assertEquals(0, repository.addCalls)
        }

    @Test
    fun creatingReminderSavesTimeAndWeekdayMask() =
        runTest {
            val model = createModel()
            model.startAdding()
            model.changeTitle("Take medicine")
            model.changeTime(7, 5)
            model.toggleWeekday(ReminderWeekday.SATURDAY.bit)
            model.save()
            runCurrent()

            assertEquals(
                listOf(
                    Reminder(
                        id = 0,
                        title = "Take medicine",
                        hour = 7,
                        minute = 5,
                        daysOfWeekMask = Reminder.ALL_DAYS_MASK xor ReminderWeekday.SATURDAY.bit,
                    ),
                ),
                model.state.value.reminders,
            )
        }

    @Test
    fun loadingFailureCanBeRetriedWithoutLosingStoredTasks() =
        runTest {
            repository.rows.value = listOf(Reminder(id = 1, title = "Stored"))
            repository.loadFails = true
            val model = createModel()
            assertEquals(LoadingStatus.LOADING, model.state.value.loadingStatus)
            runCurrent()
            assertEquals(LoadingStatus.ERROR, model.state.value.loadingStatus)
            model.startAdding()
            model.changeTitle("Keep this draft")
            repository.loadFails = false
            model.loadReminders()
            assertEquals(LoadingStatus.LOADING, model.state.value.loadingStatus)
            runCurrent()
            assertEquals(LoadingStatus.READY, model.state.value.loadingStatus)
            assertEquals(repository.rows.value, model.state.value.reminders)
            assertEquals(AddReminderState(title = "Keep this draft"), model.state.value.editor)
        }

    @Test
    fun emptyOpenEditorIsRestoredButCancelledEditorIsNot() =
        runTest {
            val savedState = SavedStateHandle()
            val original = createModel(savedState)
            assertNull(original.state.value.editor)
            original.startAdding()
            val restored = createModel(copySavedState(savedState))
            assertEquals(AddReminderState(), restored.state.value.editor)

            original.cancelAdding()
            assertNull(createModel(copySavedState(savedState)).state.value.editor)
            original.startAdding()
            assertEquals(AddReminderState(), original.state.value.editor)
            runCurrent()
            assertEquals(0, repository.addCalls)
        }

    @Test
    fun editingAfterFailureClearsErrorAndKeepsNewTitle() =
        runTest {
            repository.saveFails = true
            val model = createModel()
            model.startAdding()
            model.changeTitle("Read")
            model.save()
            runCurrent()
            assertEquals(
                SavingStatus.ERROR,
                model.state.value.editor
                    ?.status,
            )

            model.changeTitle("Read a book")
            assertEquals(AddReminderState(title = "Read a book"), model.state.value.editor)
        }

    @Test
    fun editingReminderUpdatesItsTitleAndPreservesItsId() =
        runTest {
            val reminder = Reminder(id = 7, title = "Read")
            repository.rows.value = listOf(reminder)
            val model = createModel()
            runCurrent()

            model.startEditing(reminder)
            model.changeTitle("  Read a book  ")
            model.save()
            runCurrent()

            assertEquals(listOf(Reminder(id = 7, title = "Read a book")), model.state.value.reminders)
            assertEquals(1, repository.updateCalls)
            assertEquals(0, repository.addCalls)
            assertNull(model.state.value.editor)
        }

        @Test
        fun editingReminderUpdatesTimeAndWeekdayMask() =
            runTest {
                val reminder =
                    Reminder(
                        id = 7,
                        title = "Read",
                        hour = 8,
                        minute = 30,
                        daysOfWeekMask = ReminderWeekday.MONDAY.bit or ReminderWeekday.FRIDAY.bit,
                    )
                repository.rows.value = listOf(reminder)
                val model = createModel()
                runCurrent()

                model.startEditing(reminder)
                model.changeTime(21, 0)
                model.toggleWeekday(ReminderWeekday.FRIDAY.bit)
                model.toggleWeekday(ReminderWeekday.SUNDAY.bit)
                model.save()
                runCurrent()

                assertEquals(
                    listOf(
                        reminder.copy(
                            hour = 21,
                            minute = 0,
                            daysOfWeekMask = ReminderWeekday.MONDAY.bit or ReminderWeekday.SUNDAY.bit,
                        ),
                    ),
                    model.state.value.reminders,
                )
                assertEquals(1, repository.updateCalls)
            }

    @Test
    fun cancellingEditReturnsToTheReminderBeingViewed() =
        runTest {
            val reminder = Reminder(id = 7, title = "Read")
            repository.rows.value = listOf(reminder)
            val model = createModel()
            runCurrent()

            model.viewReminder(reminder)
            model.startEditing(reminder)
            model.cancelAdding()

            assertNull(model.state.value.editor)
            assertEquals(reminder.id, model.state.value.viewingReminderId)
        }

    @Test
    fun viewingReminderCanBeRestoredAfterRecreation() =
        runTest {
            val reminder = Reminder(id = 7, title = "Read")
            repository.rows.value = listOf(reminder)
            val savedState = SavedStateHandle()
            val original = createModel(savedState)
            runCurrent()
            original.viewReminder(reminder)

            val restored = createModel(copySavedState(savedState))
            runCurrent()

            assertEquals(reminder.id, restored.state.value.viewingReminderId)
        }

    @Test
    fun addingFromReminderViewClosesTheView() =
        runTest {
            val reminder = Reminder(id = 7, title = "Read")
            repository.rows.value = listOf(reminder)
            val model = createModel()
            runCurrent()
            model.viewReminder(reminder)

            model.startAdding()

            assertEquals(AddReminderState(), model.state.value.editor)
            assertNull(model.state.value.viewingReminderId)
        }

    @Test
    fun failedEditKeepsDraftAndAllowsRetry() =
        runTest {
            val reminder = Reminder(id = 7, title = "Read")
            repository.rows.value = listOf(reminder)
            repository.updateFails = true
            val model = createModel()
            runCurrent()
            model.startEditing(reminder)
            model.changeTitle("Read a book")
            model.save()
            runCurrent()

            assertEquals(
                AddReminderState(title = "Read a book", status = SavingStatus.ERROR, reminderId = 7),
                model.state.value.editor,
            )
            repository.updateFails = false
            model.save()
            runCurrent()
            assertEquals(listOf(Reminder(id = 7, title = "Read a book")), model.state.value.reminders)
            assertNull(model.state.value.editor)
        }

    @Test
    fun deletionRequiresConfirmationAndCanBeRetriedAfterFailure() =
        runTest {
            val reminder = Reminder(id = 7, title = "Read")
            repository.rows.value = listOf(reminder)
            repository.deleteFails = true
            val model = createModel()
            runCurrent()

            model.requestDelete(reminder)
            model.cancelDelete()
            assertNull(model.state.value.deleteConfirmation)

            model.requestDelete(reminder)
            model.deleteReminder()
            runCurrent()
            assertEquals(DeletingStatus.ERROR, model.state.value.deleteConfirmation?.status)
            assertEquals(listOf(reminder), model.state.value.reminders)

            repository.deleteFails = false
            model.deleteReminder()
            runCurrent()
            assertNull(model.state.value.deleteConfirmation)
            assertEquals(emptyList<Reminder>(), model.state.value.reminders)
            assertNull(model.state.value.viewingReminderId)
            assertEquals(2, repository.deleteCalls)
        }

    private fun copySavedState(savedState: SavedStateHandle): SavedStateHandle =
        SavedStateHandle(savedState.keys().associateWith { savedState.get<Any?>(it) })

    private fun createModel(savedState: SavedStateHandle = SavedStateHandle()): RemindersViewModel =
        RemindersViewModel(repository, savedState).also {
            viewModelStore.put("model-${modelCount++}", it)
        }

    private class FakeReminderRepository : ReminderRepository {
        val rows = MutableStateFlow<List<Reminder>>(emptyList())
        var saveFails = false
        var loadFails = false
        var addCalls = 0
        var updateCalls = 0
        var deleteCalls = 0
        var saveGate: CompletableDeferred<Unit>? = null
        var updateFails = false
        var deleteFails = false

        override val reminders: Flow<List<Reminder>>
            get() =
                flow {
                    if (loadFails) error("Read failed")
                    emitAll(rows)
                }

        override suspend fun add(reminder: Reminder) {
            addCalls++
            saveGate?.await()
            if (saveFails) error("Write failed")
            rows.value += reminder
        }

        override suspend fun update(reminder: Reminder) {
            updateCalls++
            if (updateFails) error("Update failed")
            rows.value = rows.value.map { if (it.id == reminder.id) reminder else it }
        }

        override suspend fun delete(id: Long) {
            deleteCalls++
            if (deleteFails) error("Delete failed")
            rows.value = rows.value.filterNot { it.id == id }
        }
    }
}

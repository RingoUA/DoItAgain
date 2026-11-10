package com.r8.doitagain

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.r8.doitagain.data.ReminderDatabase
import com.r8.doitagain.data.Reminder
import com.r8.doitagain.data.ReminderWeekday
import com.r8.doitagain.data.RoomReminderRepository
import com.r8.doitagain.reminders.ReminderAlarmScheduler
import com.r8.doitagain.ui.RemindersApp
import com.r8.doitagain.ui.RemindersViewModel
import com.r8.doitagain.ui.theme.DoItAgainTheme
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddReminderFlowTest {
    @get:Rule
    val composeRule = createComposeRule()
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val store = ViewModelStore()
    private lateinit var database: ReminderDatabase
    private lateinit var model: RemindersViewModel

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, ReminderDatabase::class.java).build()
        model =
            RemindersViewModel(
                RoomReminderRepository(database.reminderDao()),
                SavedStateHandle(),
                NoOpReminderAlarmScheduler,
            )
        store.put("test", model)
        composeRule.setContent {
            DoItAgainTheme { RemindersApp(model) }
        }
    }

    @After
    fun tearDown() {
        composeRule.runOnIdle { store.clear() }
        database.close()
    }

    @Test
    fun savingNewTitleReturnsToListAndShowsStoredTask() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.add_reminder)).performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("  Власна назва  ")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.size == 1
        }
        composeRule.onNodeWithText(context.getString(R.string.home_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Власна назва").assertIsDisplayed()
    }

    @Test
    fun reminderCanBeViewedEditedAndDeleted() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.add_reminder)).performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Old title")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.size == 1
        }

        composeRule.onNodeWithText("Old title").performClick()
        composeRule.onNodeWithText("Old title").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.back)).assertIsDisplayed()
        val detailTitleBottom =
            composeRule.onNodeWithText("Old title").fetchSemanticsNode().boundsInRoot.bottom
        val detailActionsTop =
            composeRule.onNodeWithText(context.getString(R.string.edit)).fetchSemanticsNode().boundsInRoot.top
        assertTrue("Detail actions should be below the reminder content", detailActionsTop > detailTitleBottom)

        composeRule.onNodeWithText(context.getString(R.string.edit)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.edit_reminder)).assertIsDisplayed()
        val editorFieldBottom =
            composeRule.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot.bottom
        val editorActionsTop =
            composeRule.onNodeWithText(context.getString(R.string.cancel)).fetchSemanticsNode().boundsInRoot.top
        assertTrue("Editor actions should be below the editable fields", editorActionsTop > editorFieldBottom)

        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput("Updated title")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.singleOrNull()?.title == "Updated title"
        }
        composeRule.onNodeWithText("Updated title").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.back)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.delete)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.delete_reminder_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.cancel)).performClick()
        composeRule.onNodeWithText("Updated title").assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.delete)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.delete_reminder_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.confirm_delete)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.reminders.isEmpty() && model.state.value.deleteConfirmation == null
        }
        composeRule.onNodeWithText(context.getString(R.string.home_title)).assertIsDisplayed()
    }

    @Test
    fun cancellingEditReturnsToReminderDetailsAndBackReturnsToList() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.add_reminder)).performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Keep this title")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.size == 1
        }

        composeRule.onNodeWithText("Keep this title").performClick()
        composeRule.onNodeWithText(context.getString(R.string.edit)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.cancel)).performClick()

        composeRule.onNodeWithText("Keep this title").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.edit)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.back)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.home_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Keep this title").assertIsDisplayed()
    }

    @Test
    fun reminderScheduleCanBeAddedViewedAndEdited() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.add_reminder)).performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Take medicine")
        composeRule.onNodeWithText(context.getString(R.string.saturday_short)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.size == 1
        }

        val saved = model.state.value.reminders.single()
        assertEquals(Reminder.DEFAULT_REMINDER_HOUR, saved.hour)
        assertEquals(0, saved.minute)
        assertEquals(Reminder.ALL_DAYS_MASK xor ReminderWeekday.SATURDAY.bit, saved.daysOfWeekMask)
        composeRule.onNodeWithText("Take medicine").performClick()
        composeRule.onNodeWithText(context.getString(R.string.reminder_time)).assertIsDisplayed()
        composeRule.onNodeWithText("09:00").assertIsDisplayed().assertIsNotEnabled()
        val weekdayLabels =
            listOf(
                R.string.monday_short,
                R.string.tuesday_short,
                R.string.wednesday_short,
                R.string.thursday_short,
                R.string.friday_short,
                R.string.saturday_short,
                R.string.sunday_short,
            ).map(context::getString)
        weekdayLabels.forEach { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed().assertIsNotEnabled()
        }
        composeRule.onNodeWithText(context.getString(R.string.monday_short)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.saturday_short)).assertIsNotSelected()

        composeRule.onNodeWithText(context.getString(R.string.edit)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.saturday_short)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.friday_short)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null &&
                model.state.value.reminders.singleOrNull()?.daysOfWeekMask ==
                (Reminder.ALL_DAYS_MASK xor ReminderWeekday.FRIDAY.bit)
        }

        composeRule.onNodeWithText("09:00").assertIsDisplayed()
        composeRule.onNodeWithText("Take medicine").assertIsDisplayed()
    }

    @Test
    fun allWeekdayButtonsFitInOneFullWidthRow() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.add_reminder)).performClick()

        val weekdayLabels =
            listOf(
                R.string.monday_short,
                R.string.tuesday_short,
                R.string.wednesday_short,
                R.string.thursday_short,
                R.string.friday_short,
                R.string.saturday_short,
                R.string.sunday_short,
            ).map(context::getString)
        val bounds = weekdayLabels.map { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        }
        assertEquals(1, bounds.map { it.top }.distinct().size)
        assertTrue(bounds.first().left >= 0f)
        assertTrue(bounds.first().left < bounds.last().left)
        assertTrue(bounds.last().right > bounds.first().right)
    }
}

private object NoOpReminderAlarmScheduler : ReminderAlarmScheduler {
    override fun scheduleAll(reminders: List<Reminder>) = Unit

    override fun schedule(reminder: Reminder) = Unit

    override fun cancel(reminderId: Long) = Unit
}

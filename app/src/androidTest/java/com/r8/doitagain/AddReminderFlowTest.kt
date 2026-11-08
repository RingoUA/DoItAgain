package com.r8.doitagain

import androidx.compose.ui.test.assertIsDisplayed
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
import com.r8.doitagain.data.RoomReminderRepository
import com.r8.doitagain.ui.RemindersApp
import com.r8.doitagain.ui.RemindersViewModel
import com.r8.doitagain.ui.theme.DoItAgainTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        model = RemindersViewModel(RoomReminderRepository(database.reminderDao()), SavedStateHandle())
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
    fun reminderCanBeEditedAndDeletedFromTheList() {
        composeRule.onNodeWithContentDescription(context.getString(R.string.add_reminder)).performClick()
        composeRule.onNode(hasSetTextAction()).performTextInput("Old title")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.size == 1
        }

        composeRule.onNodeWithText("Old title").performClick()
        composeRule.onNodeWithText(context.getString(R.string.edit)).performClick()
        composeRule.onNode(hasSetTextAction()).performTextClearance()
        composeRule.onNode(hasSetTextAction()).performTextInput("Updated title")
        composeRule.onNodeWithText(context.getString(R.string.save)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.editor == null && model.state.value.reminders.singleOrNull()?.title == "Updated title"
        }

        composeRule.onNodeWithText(context.getString(R.string.delete)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.delete_reminder_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.confirm_delete)).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            model.state.value.reminders.isEmpty() && model.state.value.deleteConfirmation == null
        }
    }
}

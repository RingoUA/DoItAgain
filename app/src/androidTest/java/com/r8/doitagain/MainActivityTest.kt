package com.r8.doitagain

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchShowsHeadingAndAddButton() {
        composeRule
            .onNodeWithText(composeRule.activity.getString(R.string.home_title))
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(text(R.string.add_reminder)).assertIsDisplayed()
    }

    @Test
    fun addingDraftSurvivesRecreationAndCancelReturnsToList() {
        composeRule.onNodeWithContentDescription(text(R.string.add_reminder)).performClick()
        composeRule.onNodeWithText(text(R.string.save)).assertIsNotEnabled()
        composeRule.onNode(hasSetTextAction()).performTextInput("My draft")
        composeRule.activityRule.scenario.recreate()
        composeRule.onNode(hasSetTextAction()).assertTextContains("My draft")
        composeRule.onNodeWithText(text(R.string.cancel)).performClick()
        composeRule.onNodeWithText(text(R.string.home_title)).assertIsDisplayed()
        composeRule.onNodeWithText("My draft").assertDoesNotExist()
    }

    private fun text(resourceId: Int) = composeRule.activity.getString(resourceId)
}

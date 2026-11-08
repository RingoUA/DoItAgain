package com.r8.doitagain.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun RemindersApp(viewModel: RemindersViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val editor = state.editor
    BackHandler(enabled = editor != null) { viewModel.cancelAdding() }

    if (editor != null) {
        AddReminderScreen(
            state = editor,
            onTitleChange = viewModel::changeTitle,
            onSave = viewModel::save,
            onCancel = viewModel::cancelAdding,
        )
    } else {
        HomeScreen(
            reminders = state.reminders,
            onAddReminder = viewModel::startAdding,
            loadingStatus = state.loadingStatus,
            onRetry = viewModel::loadReminders,
        )
    }
}

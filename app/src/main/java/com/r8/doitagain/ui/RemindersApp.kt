package com.r8.doitagain.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun RemindersApp(viewModel: RemindersViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val editor = state.editor
    val viewedReminder = state.reminders.find { it.id == state.viewingReminderId }
    BackHandler(enabled = editor != null || viewedReminder != null) {
        if (editor != null) {
            viewModel.cancelAdding()
        } else {
            viewModel.closeReminderView()
        }
    }

    if (editor != null) {
        AddReminderScreen(
            state = editor,
            onTitleChange = viewModel::changeTitle,
            onTimeChange = viewModel::changeTime,
            onToggleWeekday = viewModel::toggleWeekday,
            onSave = viewModel::save,
            onCancel = viewModel::cancelAdding,
        )
    } else if (viewedReminder != null) {
        ReminderDetailScreen(
            reminder = viewedReminder,
            deleteConfirmation = state.deleteConfirmation,
            onBack = viewModel::closeReminderView,
            onEdit = { viewModel.startEditing(viewedReminder) },
            onDelete = viewModel::requestDelete,
            onConfirmDelete = viewModel::deleteReminder,
            onCancelDelete = viewModel::cancelDelete,
        )
    } else {
        HomeScreen(
            reminders = state.reminders,
            onAddReminder = viewModel::startAdding,
            onOpenReminder = viewModel::viewReminder,
            loadingStatus = state.loadingStatus,
            onRetry = viewModel::loadReminders,
        )
    }
}

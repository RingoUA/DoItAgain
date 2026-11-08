package com.r8.doitagain.ui

import com.r8.doitagain.data.Reminder

internal enum class LoadingStatus {
    LOADING,
    READY,
    ERROR,
}

internal enum class SavingStatus {
    EDITING,
    SAVING,
    ERROR,
}

internal data class AddReminderState(
    val title: String = "",
    val status: SavingStatus = SavingStatus.EDITING,
    val reminderId: Long? = null,
)

internal enum class DeletingStatus {
    CONFIRMING,
    DELETING,
    ERROR,
}

internal data class DeleteReminderState(
    val reminder: Reminder,
    val status: DeletingStatus = DeletingStatus.CONFIRMING,
)

internal data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val loadingStatus: LoadingStatus = LoadingStatus.LOADING,
    val editor: AddReminderState? = null,
    val viewingReminderId: Long? = null,
    val deleteConfirmation: DeleteReminderState? = null,
)

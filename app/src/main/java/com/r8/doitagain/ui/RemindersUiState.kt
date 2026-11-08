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
)

internal data class RemindersUiState(
    val reminders: List<Reminder> = emptyList(),
    val loadingStatus: LoadingStatus = LoadingStatus.LOADING,
    val editor: AddReminderState? = null,
)

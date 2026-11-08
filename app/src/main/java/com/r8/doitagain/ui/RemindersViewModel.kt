package com.r8.doitagain.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.r8.doitagain.data.Reminder
import com.r8.doitagain.data.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RemindersViewModel
    @Inject
    constructor(
        private val repository: ReminderRepository,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val mutableState =
            MutableStateFlow(
                RemindersUiState(
                    viewingReminderId = savedStateHandle.get<Long>(VIEWING_REMINDER_ID),
                    editor =
                        savedStateHandle.get<String>(EDITOR_TITLE)?.let {
                            AddReminderState(
                                title = it,
                                reminderId = savedStateHandle.get<Long>(EDITOR_REMINDER_ID),
                            )
                        },
                ),
            )
        val state = mutableState.asStateFlow()
        private var loadJob: Job? = null

        init {
            loadReminders()
        }

        fun loadReminders() {
            loadJob?.cancel()
            mutableState.update { it.copy(loadingStatus = LoadingStatus.LOADING) }
            loadJob =
                viewModelScope.launch {
                    repository.reminders
                        .catch { mutableState.update { it.copy(loadingStatus = LoadingStatus.ERROR) } }
                        .collect { reminders ->
                            mutableState.update {
                                it.copy(reminders = reminders, loadingStatus = LoadingStatus.READY)
                            }
                        }
                }
        }

        fun startAdding() {
            if (state.value.editor != null) return
            closeReminderView()
            savedStateHandle[EDITOR_TITLE] = ""
            savedStateHandle.remove<Long>(EDITOR_REMINDER_ID)
            mutableState.update { it.copy(editor = AddReminderState()) }
        }

        fun startEditing(reminder: Reminder) {
            if (state.value.editor != null) return
            savedStateHandle[EDITOR_TITLE] = reminder.title
            savedStateHandle[EDITOR_REMINDER_ID] = reminder.id
            mutableState.update {
                it.copy(editor = AddReminderState(title = reminder.title, reminderId = reminder.id))
            }
        }

        fun viewReminder(reminder: Reminder) {
            if (state.value.editor != null) return
            savedStateHandle[VIEWING_REMINDER_ID] = reminder.id
            mutableState.update { it.copy(viewingReminderId = reminder.id) }
        }

        fun closeReminderView() {
            savedStateHandle.remove<Long>(VIEWING_REMINDER_ID)
            mutableState.update { it.copy(viewingReminderId = null) }
        }

        fun changeTitle(title: String) {
            val editor = state.value.editor ?: return
            if (editor.status == SavingStatus.SAVING) return
            savedStateHandle[EDITOR_TITLE] = title
            mutableState.update {
                it.copy(editor = editor.copy(title = title, status = SavingStatus.EDITING))
            }
        }

        fun cancelAdding() {
            if (state.value.editor?.status == SavingStatus.SAVING) return
            closeEditor()
        }

        fun save() {
            val editor = state.value.editor ?: return
            if (editor.status == SavingStatus.SAVING || editor.title.isBlank()) return
            val title = editor.title.trim()
            mutableState.update { it.copy(editor = editor.copy(status = SavingStatus.SAVING)) }
            viewModelScope.launch {
                try {
                    val reminder = Reminder(id = editor.reminderId ?: 0, title = title)
                    if (editor.reminderId == null) {
                        repository.add(reminder)
                    } else {
                        repository.update(reminder)
                    }
                    closeEditor()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update { it.copy(editor = editor.copy(status = SavingStatus.ERROR)) }
                }
            }
        }

        fun requestDelete(reminder: Reminder) {
            if (state.value.deleteConfirmation != null) return
            mutableState.update { it.copy(deleteConfirmation = DeleteReminderState(reminder)) }
        }

        fun cancelDelete() {
            if (state.value.deleteConfirmation?.status == DeletingStatus.DELETING) return
            mutableState.update { it.copy(deleteConfirmation = null) }
        }

        fun deleteReminder() {
            val deletion = state.value.deleteConfirmation ?: return
            if (deletion.status == DeletingStatus.DELETING) return
            mutableState.update {
                it.copy(deleteConfirmation = deletion.copy(status = DeletingStatus.DELETING))
            }
            viewModelScope.launch {
                try {
                    repository.delete(deletion.reminder.id)
                    if (state.value.viewingReminderId == deletion.reminder.id) {
                        closeReminderView()
                    }
                    mutableState.update { it.copy(deleteConfirmation = null) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutableState.update {
                        it.copy(deleteConfirmation = deletion.copy(status = DeletingStatus.ERROR))
                    }
                }
            }
        }

        private fun closeEditor() {
            savedStateHandle.remove<String>(EDITOR_TITLE)
            savedStateHandle.remove<Long>(EDITOR_REMINDER_ID)
            mutableState.update { it.copy(editor = null) }
        }

        private companion object {
            const val EDITOR_TITLE = "editorTitle"
            const val EDITOR_REMINDER_ID = "editorReminderId"
            const val VIEWING_REMINDER_ID = "viewingReminderId"
        }
    }

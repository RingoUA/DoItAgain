package com.r8.doitagain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.r8.doitagain.R
import com.r8.doitagain.data.Reminder

@Composable
internal fun ReminderDetailScreen(
    reminder: Reminder,
    deleteConfirmation: DeleteReminderState?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (Reminder) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                Text(stringResource(R.string.reminder_time))
                ReminderTimeButton(
                    hour = reminder.hour,
                    minute = reminder.minute,
                    enabled = false,
                    onClick = {},
                )
                Text(stringResource(R.string.reminder_days))
                WeekdaySelector(
                    daysOfWeekMask = reminder.daysOfWeekMask,
                    enabled = false,
                    onToggleWeekday = {},
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.edit))
                }
                Button(
                    onClick = { onDelete(reminder) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }

    deleteConfirmation?.let { deletion ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(stringResource(R.string.delete_reminder_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.delete_reminder_message, deletion.reminder.title))
                    if (deletion.status == DeletingStatus.ERROR) {
                        Text(
                            text = stringResource(R.string.delete_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    enabled = deletion.status != DeletingStatus.DELETING,
                ) {
                    if (deletion.status == DeletingStatus.DELETING) {
                        CircularProgressIndicator(modifier = Modifier.padding(horizontal = 8.dp).size(20.dp))
                    } else {
                        Text(stringResource(R.string.confirm_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onCancelDelete,
                    enabled = deletion.status != DeletingStatus.DELETING,
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

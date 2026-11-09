package com.r8.doitagain.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.r8.doitagain.R
import com.r8.doitagain.ui.theme.DoItAgainTheme

@Composable
internal fun AddReminderScreen(
    state: AddReminderState,
    onTitleChange: (String) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    onToggleWeekday: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canEdit = state.status != SavingStatus.SAVING
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text =
                        stringResource(
                            if (state.reminderId == null) R.string.add_reminder else R.string.edit_reminder,
                        ),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text(stringResource(R.string.reminder_title)) },
                    singleLine = true,
                    enabled = canEdit,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (state.title.isNotBlank() && canEdit) {
                                keyboardController?.hide()
                                onSave()
                            }
                        },
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
                Text(
                    text = stringResource(R.string.reminder_time),
                    style = MaterialTheme.typography.titleMedium,
                )
                ReminderTimeButton(
                    hour = state.hour,
                    minute = state.minute,
                    enabled = canEdit,
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> onTimeChange(hour, minute) },
                            state.hour,
                            state.minute,
                            true,
                        ).show()
                    },
                )
                Text(
                    text = stringResource(R.string.reminder_days),
                    style = MaterialTheme.typography.titleMedium,
                )
                WeekdaySelector(
                    daysOfWeekMask = state.daysOfWeekMask,
                    enabled = canEdit,
                    onToggleWeekday = onToggleWeekday,
                )
                if (state.status == SavingStatus.ERROR) {
                    Text(
                        text = stringResource(R.string.save_error),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End),
            ) {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        onCancel()
                    },
                    enabled = canEdit,
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onSave()
                    },
                    enabled = state.title.isNotBlank() && canEdit,
                ) {
                    if (state.status == SavingStatus.SAVING) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Preview(name = "Add reminder", showBackground = true, locale = "en")
@Preview(name = "Додавання задачі", showBackground = true, locale = "uk")
@Composable
private fun AddReminderScreenPreview() {
    DoItAgainTheme {
        AddReminderScreen(
            state = AddReminderState(),
            onTitleChange = {},
            onTimeChange = { _, _ -> },
            onToggleWeekday = {},
            onSave = {},
            onCancel = {},
        )
    }
}

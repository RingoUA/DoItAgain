package com.r8.doitagain.ui

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
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canEdit = state.status != SavingStatus.SAVING
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = stringResource(R.string.add_reminder),
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
                    onDone = { if (state.title.isNotBlank() && canEdit) onSave() },
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
            if (state.status == SavingStatus.ERROR) {
                Text(
                    text = stringResource(R.string.save_error),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onCancel, enabled = canEdit) {
                    Text(stringResource(R.string.cancel))
                }
                Button(onClick = onSave, enabled = state.title.isNotBlank() && canEdit) {
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
            onSave = {},
            onCancel = {},
        )
    }
}

package com.r8.doitagain.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.r8.doitagain.R
import com.r8.doitagain.data.Reminder
import com.r8.doitagain.ui.theme.DoItAgainTheme

@Composable
internal fun HomeScreen(
    reminders: List<Reminder>,
    onAddReminder: () -> Unit,
    onOpenReminder: (Reminder) -> Unit = {},
    modifier: Modifier = Modifier,
    loadingStatus: LoadingStatus = LoadingStatus.READY,
    onRetry: () -> Unit = {},
) {
    val addReminderDescription = stringResource(R.string.add_reminder)

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder,
                modifier =
                    Modifier.semantics {
                        contentDescription = addReminderDescription
                    },
            ) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
            if (loadingStatus == LoadingStatus.LOADING) {
                item { CircularProgressIndicator() }
            }
            if (loadingStatus == LoadingStatus.ERROR) {
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.load_error),
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            items(reminders, key = { it.id }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onClick = { onOpenReminder(reminder) },
                )
            }
        }
    }
}

@Composable
@Suppress("ktlint:standard:function-naming")
private fun ReminderCard(reminder: Reminder, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(8.dp).semantics { heading() },
            )
        }
    }
}

@Preview(name = "English", showBackground = true, locale = "en")
@Preview(name = "Ukrainian", showBackground = true, locale = "uk")
@Preview(name = "Dark", showBackground = true, locale = "en", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large text", showBackground = true, locale = "uk", fontScale = 1.5f)
@Composable
private fun HomeScreenPreview() {
    DoItAgainTheme {
        HomeScreen(reminders = emptyList(), onAddReminder = {})
    }
}

@Preview(name = "Sample reminders", showBackground = true, locale = "en")
@Composable
private fun HomeScreenWithRemindersPreview() {
    DoItAgainTheme {
        HomeScreen(
            reminders =
                listOf(
                    Reminder(
                        id = 1,
                        title = "Reminder 1",
                    ),
                    Reminder(
                        id = 2,
                        title = "Reminder 2",
                    ),
                    Reminder(
                        id = 3,
                        title = "Reminder 3",
                    ),
                ),
            onAddReminder = {},
        )
    }
}

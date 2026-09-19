package com.r8.doitagain.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.r8.doitagain.R
import com.r8.doitagain.ui.theme.DoItAgainTheme
import kotlin.collections.listOf

@Composable
internal fun HomeScreen(
    reminders: List<Reminder>,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() },
                )
            }
            items(reminders, key = { it.id }) { reminder ->
                ReminderCard(reminder = reminder)
            }
        }
    }
}

@Composable
private fun ReminderCard(reminder: Reminder) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = reminder.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(20.dp).semantics { heading() },
        )
    }
}

@Preview(name = "English", showBackground = true, locale = "en")
@Preview(name = "Ukrainian", showBackground = true, locale = "uk")
@Preview(name = "Dark", showBackground = true, locale = "en", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large text", showBackground = true, locale = "uk", fontScale = 1.5f)
@Composable
private fun HomeScreenPreview() {
    DoItAgainTheme {
        HomeScreen(reminders = emptyList())
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
                        id = "reminder_1",
                        title = "Reminder 1",
                    ),
                    Reminder(
                        id = "reminder_2",
                        title = "Reminder 2",
                    ),
                    Reminder(
                        id = "reminder_3",
                        title = "Reminder 3",
                    ),
                ),
        )
    }
}

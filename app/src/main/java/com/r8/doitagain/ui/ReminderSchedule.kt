package com.r8.doitagain.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.r8.doitagain.R
import com.r8.doitagain.data.ReminderWeekday

@Composable
internal fun ReminderTimeButton(
    hour: Int,
    minute: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(60.dp),
    ) {
        Text(
            stringResource(R.string.time_format, hour, minute),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
internal fun WeekdaySelector(
    daysOfWeekMask: Int,
    enabled: Boolean,
    onToggleWeekday: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ReminderWeekday.entries.forEach { weekday ->
            FilterChip(
                selected = daysOfWeekMask and weekday.bit != 0,
                onClick = { onToggleWeekday(weekday.bit) },
                enabled = enabled,
                label = { Text(weekdayAbbreviation(weekday), maxLines = 1) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun weekdayAbbreviation(weekday: ReminderWeekday): String =
    stringResource(
        when (weekday) {
            ReminderWeekday.MONDAY -> R.string.monday_short
            ReminderWeekday.TUESDAY -> R.string.tuesday_short
            ReminderWeekday.WEDNESDAY -> R.string.wednesday_short
            ReminderWeekday.THURSDAY -> R.string.thursday_short
            ReminderWeekday.FRIDAY -> R.string.friday_short
            ReminderWeekday.SATURDAY -> R.string.saturday_short
            ReminderWeekday.SUNDAY -> R.string.sunday_short
        },
    )

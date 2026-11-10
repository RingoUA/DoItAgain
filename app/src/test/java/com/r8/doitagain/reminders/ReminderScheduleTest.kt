package com.r8.doitagain.reminders

import com.r8.doitagain.data.Reminder
import com.r8.doitagain.data.ReminderWeekday
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderScheduleTest {
    private val utc = ZoneId.of("UTC")

    @Test
    fun picksNextOccurrenceOnSelectedWeekday() {
        val reminder =
            Reminder(
                title = "Read",
                hour = 9,
                minute = 30,
                daysOfWeekMask = ReminderWeekday.WEDNESDAY.bit,
            )

        assertEquals(
            Instant.parse("2026-11-11T09:30:00Z"),
            nextReminderTime(reminder, Instant.parse("2026-11-10T10:00:00Z"), utc),
        )
    }

    @Test
    fun sameDayTimeInPastMovesToNextWeek() {
        val reminder =
            Reminder(
                title = "Read",
                hour = 9,
                minute = 30,
                daysOfWeekMask = ReminderWeekday.TUESDAY.bit,
            )

        assertEquals(
            Instant.parse("2026-11-17T09:30:00Z"),
            nextReminderTime(reminder, Instant.parse("2026-11-10T09:30:01Z"), utc),
        )
    }

    @Test
    fun noSelectedDaysHasNoNextOccurrence() {
        assertNull(
            nextReminderTime(
                Reminder(title = "Read", daysOfWeekMask = 0),
                Instant.parse("2026-11-10T09:00:00Z"),
                utc,
            ),
        )
    }
}

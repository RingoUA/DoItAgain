package com.r8.doitagain.data

import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTest {
    @Test
    fun acceptsValidTimeAndWeekdayMaskBoundaries() {
        assertTrue(runCatching { Reminder(title = "Start of day", hour = 0, minute = 0, daysOfWeekMask = 0) }.isSuccess)
        assertTrue(
            runCatching {
                Reminder(title = "End of day", hour = 23, minute = 59, daysOfWeekMask = Reminder.ALL_DAYS_MASK)
            }.isSuccess,
        )
    }

    @Test
    fun rejectsInvalidTimeAndWeekdayMasks() {
        assertTrue(runCatching { Reminder(title = "Invalid hour", hour = 24) }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { Reminder(title = "Invalid minute", minute = 60) }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(
            runCatching {
                Reminder(title = "Invalid weekday mask", daysOfWeekMask = Reminder.ALL_DAYS_MASK + 1)
            }.exceptionOrNull() is IllegalArgumentException,
        )
    }
}

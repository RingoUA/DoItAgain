package com.r8.doitagain.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
internal data class Reminder(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title: String,
) {
    init {
        require(title.isNotBlank()) { "Reminder title cannot be blank" }
    }
}

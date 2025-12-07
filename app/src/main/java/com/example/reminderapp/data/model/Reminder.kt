package com.example.reminderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Reminder entity stored in Room.
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val description: String,

    // date/time of the reminder
    val date: Long,          // millis since epoch (used for day filters)
    val time: String,        // "HH:mm"

    // color coding 0..4
    val colorCode: Int = 0,

    val location: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),

    // Camera image file path (if any)
    val imagePath: String = ""
)

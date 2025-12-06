package com.example.reminderapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a Reminder entity
 * This will be stored in the Room database
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String,

    // Store date as timestamp (milliseconds since epoch)
    val date: Long,

    val time: String, // Format: "HH:mm" (e.g., "14:30")

    // Color coding: 0=Blue, 1=Pink, 2=Green, 3=Yellow, etc.
    val colorCode: Int = 0,

    val location: String = "",

    val isCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),

    // NEW: Image path for camera integration
    val imagePath: String = ""
)
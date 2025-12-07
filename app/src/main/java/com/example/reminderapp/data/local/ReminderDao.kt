package com.example.reminderapp.data.local

import androidx.room.*
import com.example.reminderapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    // READ – all reminders
    @Query("SELECT * FROM reminders ORDER BY date ASC, time ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    // READ – reminders between two timestamps
    @Query(
        """
        SELECT * FROM reminders 
        WHERE date BETWEEN :startOfDay AND :endOfDay 
        ORDER BY time ASC
        """
    )
    fun getRemindersForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<Reminder>>

    // READ – single reminder
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): Reminder?

    // UPDATE
    @Update
    suspend fun updateReminder(reminder: Reminder)

    // DELETE single reminder
    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    // DELETE completed reminders
    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun deleteCompletedReminders()
}

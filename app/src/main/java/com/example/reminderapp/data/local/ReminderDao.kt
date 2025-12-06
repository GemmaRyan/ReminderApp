package com.example.reminderapp.data.local

import androidx.room.*
import com.example.reminderapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) for Reminder operations
 * Defines all database queries for CRUD operations
 */
@Dao
interface ReminderDao {

    /**
     * CREATE - Insert a new reminder
     * @return the ID of the inserted reminder
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    /**
     * READ - Get all reminders as a Flow (automatically updates UI)
     * Sorted by date and time
     */
    @Query("SELECT * FROM reminders ORDER BY date ASC, time ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    /**
     * READ - Get reminders for a specific date
     * @param startOfDay - timestamp for start of selected day
     * @param endOfDay - timestamp for end of selected day
     */
    @Query("SELECT * FROM reminders WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY time ASC")
    fun getRemindersForDate(startOfDay: Long, endOfDay: Long): Flow<List<Reminder>>

    /**
     * READ - Get a single reminder by ID
     */
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: Int): Reminder?

    /**
     * READ - Search reminders by title
     */
    @Query("SELECT * FROM reminders WHERE title LIKE '%' || :searchQuery || '%' ORDER BY date ASC")
    fun searchReminders(searchQuery: String): Flow<List<Reminder>>

    /**
     * UPDATE - Update an existing reminder
     */
    @Update
    suspend fun updateReminder(reminder: Reminder)

    /**
     * DELETE - Delete a specific reminder
     */
    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    /**
     * DELETE - Delete all completed reminders (for WorkManager cleanup task)
     */
    @Query("DELETE FROM reminders WHERE isCompleted = 1")
    suspend fun deleteCompletedReminders()
}
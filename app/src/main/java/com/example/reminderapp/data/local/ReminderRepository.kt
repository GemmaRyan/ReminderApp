package com.example.reminderapp.data.local

import com.example.reminderapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository class that handles data operations
 * Provides a clean API for the ViewModel to access data
 */
class ReminderRepository(private val reminderDao: ReminderDao) {

    /**
     * Get all reminders from database
     * Returns Flow so UI updates automatically when data changes
     */
    fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders()
    }

    /**
     * Get reminders for a specific date
     * @param dateInMillis - the date in milliseconds since epoch
     */
    fun getRemindersForDate(dateInMillis: Long): Flow<List<Reminder>> {
        // Calculate start and end of day
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateInMillis

        // Start of day (00:00:00)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // End of day (23:59:59)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return reminderDao.getRemindersForDate(startOfDay, endOfDay)
    }

    /**
     * Get reminders for today
     */
    fun getTodayReminders(): Flow<List<Reminder>> {
        return getRemindersForDate(System.currentTimeMillis())
    }

    /**
     * Get a single reminder by ID
     */
    suspend fun getReminderById(id: Int): Reminder? {
        return reminderDao.getReminderById(id)
    }

    /**
     * Search reminders by title
     */
    fun searchReminders(query: String): Flow<List<Reminder>> {
        return reminderDao.searchReminders(query)
    }

    /**
     * Insert a new reminder
     * Returns the ID of the inserted reminder
     */
    suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    /**
     * Update an existing reminder
     */
    suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder)
    }

    /**
     * Delete a reminder
     */
    suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder)
    }

    /**
     * Delete all completed reminders
     * Used by WorkManager for cleanup
     */
    suspend fun deleteCompletedReminders() {
        reminderDao.deleteCompletedReminders()
    }
}
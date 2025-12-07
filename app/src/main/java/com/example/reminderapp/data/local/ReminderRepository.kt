package com.example.reminderapp.data.local

import com.example.reminderapp.data.model.Reminder
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Repository for all CRUD logic + date range handling.
 */
class ReminderRepository(
    private val reminderDao: ReminderDao
) {

    // GET ALL reminders
    fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders()

    // GET reminders for a specific date
    fun getRemindersForDate(dateInMillis: Long): Flow<List<Reminder>> {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateInMillis }

        // Start of day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // End of day
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return reminderDao.getRemindersForDateRange(startOfDay, endOfDay)
    }

    // GET single reminder
    suspend fun getReminderById(id: Int): Reminder? =
        reminderDao.getReminderById(id)

    // CREATE
    suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder)

    // UPDATE
    suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder)

    // DELETE
    suspend fun deleteReminder(reminder: Reminder) =
        reminderDao.deleteReminder(reminder)

    // DELETE completed reminders
    suspend fun deleteCompletedReminders() =
        reminderDao.deleteCompletedReminders()
}

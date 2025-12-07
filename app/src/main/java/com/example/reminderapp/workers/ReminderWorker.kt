package com.example.reminderapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.reminderapp.data.local.ReminderDatabase
import com.example.reminderapp.data.local.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val db = ReminderDatabase.getInstance(applicationContext)
            val repo = ReminderRepository(db.reminderDao())

            repo.deleteCompletedReminders()

            Log.d("ReminderWorker", "Successfully cleaned up completed reminders")
            Result.success()
        } catch (e: Exception) {
            Log.e("ReminderWorker", "Cleanup failed: ${e.message}")
            Result.retry()
        }
    }
}

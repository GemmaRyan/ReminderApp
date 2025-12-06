package com.example.reminderapp.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.reminderapp.workers.ReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Utility class for scheduling WorkManager tasks
 * Handles periodic background work scheduling
 */
object WorkManagerScheduler {

    // Unique work names
    private const val CLEANUP_WORK_NAME = "reminder_cleanup_work"

    /**
     * Schedule periodic cleanup work
     * Runs every 24 hours to clean up completed reminders
     *
     * @param context - Application context
     */
    fun schedulePeriodicCleanup(context: Context) {
        // Define constraints - only run when device conditions are met
        val constraints = Constraints.Builder()
            .setRequiresCharging(false) // Doesn't need charging
            .setRequiresBatteryNotLow(true) // Wait for battery to not be low
            .setRequiresDeviceIdle(false) // Doesn't need device to be idle
            .build()

        // Create periodic work request - runs every 24 hours
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval = 24, // Repeat every 24 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1, // Allow 1 hour flexibility
            flexTimeIntervalUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag("cleanup") // Tag for easier management
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL, // If work fails, retry with exponential backoff
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // Enqueue the work - use KEEP to not replace if already scheduled
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            cleanupWorkRequest
        )

        Log.d("WorkManagerScheduler", "Periodic cleanup work scheduled")
    }

    /**
     * Cancel all scheduled work
     * Useful for testing or if user wants to disable background tasks
     */
    fun cancelAllWork(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
        Log.d("WorkManagerScheduler", "All work cancelled")
    }

    /**
     * Cancel only cleanup work
     */
    fun cancelCleanupWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CLEANUP_WORK_NAME)
        Log.d("WorkManagerScheduler", "Cleanup work cancelled")
    }

    /**
     * Check if cleanup work is scheduled
     */
    fun isCleanupWorkScheduled(context: Context): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(CLEANUP_WORK_NAME)
            .get()

        return workInfos.any { !it.state.isFinished }
    }
}
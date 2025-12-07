package com.example.reminderapp.utils

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.reminderapp.workers.ReminderWorker
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val CLEANUP_WORK_NAME = "reminder_cleanup_work"

    fun schedulePeriodicCleanup(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val cleanupWorkRequest =
            PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .addTag("cleanup")
                .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest
        )

        Log.d("WorkManagerScheduler", "Periodic cleanup work scheduled")
    }
}

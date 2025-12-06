package com.example.reminderapp

import android.app.Application
import android.util.Log
import com.example.reminderapp.utils.WorkManagerScheduler

/**
 * Custom Application class
 * Initializes WorkManager and other app-wide components
 */
class ReminderApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize WorkManager periodic tasks
        // This schedules background cleanup to run every 24 hours
        WorkManagerScheduler.schedulePeriodicCleanup(this)

        Log.d("ReminderApplication", "Application initialized, WorkManager scheduled")
    }
}
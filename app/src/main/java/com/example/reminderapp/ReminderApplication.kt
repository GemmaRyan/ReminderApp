package com.example.reminderapp

import android.app.Application
import com.example.reminderapp.data.local.ReminderDatabase
import com.example.reminderapp.data.local.ReminderRepository
import com.example.reminderapp.utils.PreferencesManager
import com.example.reminderapp.utils.WorkManagerScheduler

class ReminderApplication : Application() {

    lateinit var repository: ReminderRepository
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()

        val db = ReminderDatabase.getInstance(this)
        repository = ReminderRepository(db.reminderDao())
        preferencesManager = PreferencesManager(this)

        // schedule periodic cleanup work
        WorkManagerScheduler.schedulePeriodicCleanup(this)
    }
}

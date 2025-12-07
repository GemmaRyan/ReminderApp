package com.example.reminderapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.reminderapp.data.model.Reminder

@Database(
    entities = [Reminder::class],
    version = 3, // 🔥 IMPORTANT: must match existing DB version to stop the crash
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {

        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        // Migration 1 → 2 (add imagePath)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE reminders ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        // Optional: If you had a version 2 → 3 change in the past,
        // but you no longer need it, fallbackToDestructiveMigration handles it.

        fun getInstance(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    // Existing migration 1 → 2
                    .addMigrations(MIGRATION_1_2)

                    // 🔥 PREVENT ROOM FROM CRASHING ON STARTUP
                    // If schema mismatches, delete and recreate DB automatically.
                    .fallbackToDestructiveMigration()

                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

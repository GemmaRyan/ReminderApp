package com.example.reminderapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.reminderapp.data.model.Reminder

/**
 * Room Database class for the Reminder app
 * This is a singleton - only one instance exists throughout the app
 */
@Database(
    entities = [Reminder::class],
    version = 2,  // Updated version
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        // Migration from version 1 to 2 (adding imagePath column)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the imagePath column with a default empty string
                database.execSQL("ALTER TABLE reminders ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                android.util.Log.d("ReminderDatabase", "Creating database instance")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                android.util.Log.d("ReminderDatabase", "Database instance created")
                instance
            }
        }
    }
}
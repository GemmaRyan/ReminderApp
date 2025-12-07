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
    version = 2,
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {

        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        // Migration 1 → 2: add imagePath column
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE reminders ADD COLUMN imagePath TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getInstance(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "reminder_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    // If you get migration errors during dev, you *can* temporarily use:
                    // .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

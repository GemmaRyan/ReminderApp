package com.example.reminderapp.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore manager for app preferences
 * Currently manages dark mode setting
 */
class PreferencesManager(private val context: Context) {

    companion object {
        // Create DataStore instance
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "app_preferences"
        )

        // Keys for preferences
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }

    /**
     * Get dark mode preference as Flow
     * Default is true (dark mode enabled)
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: true // Default to dark mode
    }

    /**
     * Save dark mode preference
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}
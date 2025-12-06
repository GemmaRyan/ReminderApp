package com.example.reminderapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.data.local.ReminderDatabase
import com.example.reminderapp.data.local.ReminderRepository
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel for managing Reminder app state
 * Uses StateFlow for reactive UI updates (required by rubric)
 */
class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    // Repository instance
    private val repository: ReminderRepository

    // Preferences manager for dark mode
    private val preferencesManager = PreferencesManager(application)

    // StateFlow for all reminders (automatically updates UI)
    private val _allReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val allReminders: StateFlow<List<Reminder>> = _allReminders.asStateFlow()

    // StateFlow for filtered reminders (by date or search)
    private val _filteredReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val filteredReminders: StateFlow<List<Reminder>> = _filteredReminders.asStateFlow()

    // StateFlow for selected date
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    // StateFlow for search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow for loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow for error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Dark mode state
    val isDarkMode: StateFlow<Boolean> = MutableStateFlow(true)

    init {
        // Initialize database and repository
        val database = ReminderDatabase.getDatabase(application)
        repository = ReminderRepository(database.reminderDao())

        // Load dark mode preference
        viewModelScope.launch {
            preferencesManager.isDarkMode.collect { darkMode ->
                (isDarkMode as MutableStateFlow).value = darkMode
            }
        }

        // Load today's reminders on initialization
        loadTodayReminders()
    }

    /**
     * Load all reminders
     */
    fun loadAllReminders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllReminders().collect { reminders ->
                    _allReminders.value = reminders
                    _filteredReminders.value = reminders
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load reminders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load reminders for today
     */
    fun loadTodayReminders() {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("ReminderViewModel", "Loading today's reminders...")
            try {
                repository.getTodayReminders().collect { reminders ->
                    android.util.Log.d("ReminderViewModel", "Loaded ${reminders.size} reminders")
                    _filteredReminders.value = reminders
                }
            } catch (e: Exception) {
                android.util.Log.e("ReminderViewModel", "Failed to load today's reminders", e)
                _errorMessage.value = "Failed to load today's reminders: ${e.message}"
            } finally {
                _isLoading.value = false
                android.util.Log.d("ReminderViewModel", "Loading complete")
            }
        }
    }

    /**
     * Load reminders for a specific date
     */
    fun loadRemindersForDate(dateInMillis: Long) {
        _selectedDate.value = dateInMillis
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getRemindersForDate(dateInMillis).collect { reminders ->
                    _filteredReminders.value = reminders
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load reminders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search reminders by title
     */
    fun searchReminders(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                // If search is empty, show all reminders
                loadTodayReminders()
            } else {
                try {
                    repository.searchReminders(query).collect { reminders ->
                        _filteredReminders.value = reminders
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Search failed: ${e.message}"
                }
            }
        }
    }

    /**
     * Insert a new reminder
     * Validates data before inserting
     */
    fun insertReminder(
        title: String,
        description: String,
        date: Long,
        time: String,
        colorCode: Int,
        location: String,
        imagePath: String = ""
    ) {
        viewModelScope.launch {
            try {
                // Validation
                if (title.isBlank()) {
                    _errorMessage.value = "Title cannot be empty"
                    return@launch
                }

                if (date < System.currentTimeMillis() - 86400000) { // More than 1 day in the past
                    _errorMessage.value = "Cannot create reminder for past dates"
                    return@launch
                }

                val reminder = Reminder(
                    title = title.trim(),
                    description = description.trim(),
                    date = date,
                    time = time,
                    colorCode = colorCode,
                    location = location.trim(),
                    imagePath = imagePath
                )

                repository.insertReminder(reminder)
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = "Failed to create reminder: ${e.message}"
            }
        }
    }

    /**
     * Update an existing reminder
     */
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                // Validation
                if (reminder.title.isBlank()) {
                    _errorMessage.value = "Title cannot be empty"
                    return@launch
                }

                repository.updateReminder(reminder)
                _errorMessage.value = null

            } catch (e: Exception) {
                _errorMessage.value = "Failed to update reminder: ${e.message}"
            }
        }
    }
    /**
     * Insert test reminders (for debugging)
     */
    fun insertTestReminders() {
        viewModelScope.launch {
            try {
                val testReminders = listOf(
                    Reminder(
                        title = "Test Reminder 1",
                        description = "This is a test",
                        date = System.currentTimeMillis(),
                        time = "10:00",
                        colorCode = 0
                    ),
                    Reminder(
                        title = "Test Reminder 2",
                        description = "Another test",
                        date = System.currentTimeMillis(),
                        time = "14:00",
                        colorCode = 1
                    )
                )

                testReminders.forEach { reminder ->
                    repository.insertReminder(reminder)
                    android.util.Log.d("ReminderViewModel", "Inserted test reminder: ${reminder.title}")
                }

                loadTodayReminders()
            } catch (e: Exception) {
                android.util.Log.e("ReminderViewModel", "Failed to insert test reminders", e)
            }
        }
    }

    /**
     * Delete a reminder
     */
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(reminder)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete reminder: ${e.message}"
            }
        }
    }

    /**
     * Toggle dark mode
     */
    fun toggleDarkMode() {
        viewModelScope.launch {
            val newValue = !isDarkMode.value
            preferencesManager.setDarkMode(newValue)
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Get current date for display
     */
    fun getCurrentDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _selectedDate.value

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)

        return "$day $month $year"
    }

    /**
     * Get a single reminder by ID
     */
    suspend fun getReminderById(id: Int): Reminder? {
        return try {
            repository.getReminderById(id)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load reminder: ${e.message}"
            null
        }
    }

}
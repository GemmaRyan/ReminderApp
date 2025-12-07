package com.example.reminderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reminderapp.data.local.ReminderRepository
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    // ---------- DARK MODE ----------
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // ---------- ALL REMINDERS ----------
    private val _allReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val allReminders: StateFlow<List<Reminder>> = _allReminders.asStateFlow()

    // ---------- SELECTED DATE ----------
    private val _selectedDate = MutableStateFlow<Long?>(null)
    val selectedDate: StateFlow<Long?> = _selectedDate.asStateFlow()

    private val _remindersForSelectedDate = MutableStateFlow<List<Reminder>>(emptyList())
    val remindersForSelectedDate: StateFlow<List<Reminder>> = _remindersForSelectedDate.asStateFlow()

    init {
        // Observe dark mode
        viewModelScope.launch {
            prefs.isDarkMode.collect { enabled ->
                _isDarkMode.value = enabled
            }
        }

        // Observe ALL reminders from DB
        viewModelScope.launch {
            repository.getAllReminders().collect { list ->
                _allReminders.value = list
            }
        }
    }

    // ---------- DARK MODE ----------
    fun toggleDarkMode() {
        viewModelScope.launch {
            prefs.setDarkMode(!_isDarkMode.value)
        }
    }

    // ---------- DATE FILTER ----------
    fun setSelectedDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
        viewModelScope.launch {
            repository.getRemindersForDate(dateMillis).collect { list ->
                _remindersForSelectedDate.value = list
            }
        }
    }

    // ---------- CRUD ----------
    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insertReminder(reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    suspend fun getReminderById(id: Int): Reminder? {
        return repository.getReminderById(id)
    }
}

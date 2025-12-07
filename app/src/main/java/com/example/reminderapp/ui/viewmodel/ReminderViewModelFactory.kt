package com.example.reminderapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.reminderapp.data.local.ReminderRepository
import com.example.reminderapp.utils.PreferencesManager

class ReminderViewModelFactory(
    private val repository: ReminderRepository,
    private val prefs: PreferencesManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            return ReminderViewModel(repository, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

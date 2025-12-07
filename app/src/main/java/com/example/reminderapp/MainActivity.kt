package com.example.reminderapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.reminderapp.navigation.ReminderNavGraph
import com.example.reminderapp.ui.theme.ReminderAppTheme
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import com.example.reminderapp.ui.viewmodel.ReminderViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as ReminderApplication
        val factory = ReminderViewModelFactory(
            repository = app.repository,
            prefs = app.preferencesManager
        )

        setContent {
            val viewModel: ReminderViewModel = viewModel(factory = factory)
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            ReminderAppTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReminderNavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

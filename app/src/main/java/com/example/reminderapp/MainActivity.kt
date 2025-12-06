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

/**
 * Main Activity - Entry point of the app
 * Sets up Jetpack Compose with Material 3 theme and navigation
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get the ViewModel (shared across all screens)
            val viewModel: ReminderViewModel = viewModel()

            // Collect dark mode state
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            // Apply theme
            ReminderAppTheme(darkTheme = isDarkMode) {
                // Create navigation controller
                val navController = rememberNavController()

                // Surface container using the background color from theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up navigation graph
                    ReminderNavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
package com.example.reminderapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reminderapp.ui.screens.*
import com.example.reminderapp.ui.viewmodel.ReminderViewModel

/**
 * Sealed class defining all navigation routes in the app
 * This makes navigation type-safe and prevents typos
 */
sealed class Screen(val route: String) {
    object Start : Screen("start")
    object AllReminders : Screen("all_reminders")
    object Calendar : Screen("calendar")
    object AddReminder : Screen("add_reminder")
    object ViewReminder : Screen("view_reminder/{reminderId}") {
        fun createRoute(reminderId: Int) = "view_reminder/$reminderId"
    }
    object Settings : Screen("settings")
    object Camera : Screen("camera")  // NEW: Camera screen
}

/**
 * Main navigation graph for the app
 * Defines all screens and navigation paths
 *
 * @param navController - handles navigation between screens
 * @param viewModel - shared ViewModel across screens
 */
@Composable
fun ReminderNavGraph(
    navController: NavHostController,
    viewModel: ReminderViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        // Start Screen (Splash screen with logo)
        composable(route = Screen.Start.route) {
            StartScreen(
                onNavigateToMain = {
                    // Navigate to All Reminders and clear back stack
                    navController.navigate(Screen.AllReminders.route) {
                        popUpTo(Screen.Start.route) { inclusive = true }
                    }
                }
            )
        }

        // All Reminders Screen (Main hub - Today view)
        composable(route = Screen.AllReminders.route) {
            AllRemindersScreen(
                viewModel = viewModel,
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToAddReminder = {
                    navController.navigate(Screen.AddReminder.route)
                },
                onNavigateToViewReminder = { reminderId ->
                    navController.navigate(Screen.ViewReminder.createRoute(reminderId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Calendar Screen (Pick a date)
        composable(route = Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onDateSelected = {
                    // Load reminders for selected date and go back
                    navController.popBackStack()
                }
            )
        }

        // Add Reminder Screen
        composable(route = Screen.AddReminder.route) {
            AddReminderScreen(
                viewModel = viewModel,
                navController = navController,  // ADD THIS
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReminderAdded = {
                    navController.popBackStack()
                }
            )
        }

        // View Reminder Screen (with reminderId parameter)
        composable(
            route = Screen.ViewReminder.route,
            arguments = listOf(
                navArgument("reminderId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: return@composable

            ViewReminderScreen(
                reminderId = reminderId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onReminderDeleted = {
                    // Go back after deleting
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        // Camera Screen (NEW)
        composable(route = Screen.Camera.route) {
            CameraScreen(
                onPhotoTaken = { imageUri ->
                    // Save the image URI and navigate back
                    // You can pass this to Add/Edit screens via navigation arguments
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("photo_uri", imageUri)
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
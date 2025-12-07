package com.example.reminderapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.reminderapp.ui.screens.*
import com.example.reminderapp.ui.viewmodel.ReminderViewModel

sealed class Screen(val route: String) {
    object Start : Screen("start")
    object AllReminders : Screen("all_reminders")
    object Calendar : Screen("calendar")
    object AddReminder : Screen("add_reminder")
    object ViewReminder : Screen("view_reminder/{reminderId}") {
        fun createRoute(reminderId: Int) = "view_reminder/$reminderId"
    }
    object Settings : Screen("settings")
    object Camera : Screen("camera")
}

@Composable
fun ReminderNavGraph(
    navController: NavHostController,
    viewModel: ReminderViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Start.route
    ) {
        composable(Screen.Start.route) {
            StartScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.AllReminders.route) {
                        popUpTo(Screen.Start.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AllReminders.route) {
            AllRemindersScreen(
                viewModel = viewModel,
                onNavigateToAddReminder = {
                    navController.navigate(Screen.AddReminder.route)
                },
                onNavigateToViewReminder = { id ->
                    navController.navigate(Screen.ViewReminder.createRoute(id))
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.AddReminder.route) {
            AddReminderScreen(
                viewModel = viewModel,
                navController = navController,
                onReminderSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onDateSelected = {
                    // After selecting date, go back to list
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ViewReminder.route,
            arguments = listOf(
                navArgument("reminderId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: return@composable

            ViewReminderScreen(
                reminderId = reminderId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onReminderDeleted = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onPhotoTaken = { imageUri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("photo_uri", imageUri)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

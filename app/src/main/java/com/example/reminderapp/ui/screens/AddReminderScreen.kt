package com.example.reminderapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.navigation.Screen
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    navController: NavHostController,
    onReminderSaved: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("00:00") }
    var selectedColorCode by remember { mutableStateOf(0) }

    val selectedDate = remember { Calendar.getInstance() }

    val photoUri = navController
        .currentBackStackEntry
        ?.savedStateHandle
        ?.get<String>("photo_uri")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Reminder") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Date: ${Date(selectedDate.timeInMillis)}")
            Text("Time: $selectedTime")
            Text("Color code: $selectedColorCode")

            if (photoUri != null) {
                Text("Photo attached ✔")
            }

            Button(
                onClick = { navController.navigate(Screen.Camera.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Take Photo")
            }

            Button(
                onClick = {
                    if (title.isBlank()) return@Button

                    val reminder = Reminder(
                        title = title.trim(),
                        description = description.trim(),
                        date = selectedDate.timeInMillis,
                        time = selectedTime,
                        location = location.trim(),
                        colorCode = selectedColorCode,
                        imagePath = photoUri.orEmpty()
                    )

                    viewModel.insertReminder(reminder)

                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("photo_uri")

                    onReminderSaved()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Save Reminder")
            }
        }
    }
}

package com.example.reminderapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reminderapp.ui.components.ReminderCard
import com.example.reminderapp.ui.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRemindersScreen(
    viewModel: ReminderViewModel,
    onNavigateToAddReminder: () -> Unit,
    onNavigateToViewReminder: (Int) -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val reminders by viewModel.allReminders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Reminders") },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddReminder) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (reminders.isEmpty()) {
                EmptyStateContent(onAddReminder = onNavigateToAddReminder)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onClick = { onNavigateToViewReminder(reminder.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateContent(onAddReminder: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No reminders yet")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Tap the + button to create one")
    }
}

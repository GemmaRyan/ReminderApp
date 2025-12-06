package com.example.reminderapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reminderapp.ui.components.ReminderCard
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * All Reminders Screen (Main Hub - Today View)
 * Shows all reminders for the current day with search functionality
 * Matches the "Today" screen in Figma prototype
 *
 * @param viewModel - ViewModel managing app state
 * @param onNavigateToCalendar - Navigate to calendar screen
 * @param onNavigateToAddReminder - Navigate to add reminder screen
 * @param onNavigateToViewReminder - Navigate to view reminder details
 * @param onNavigateToSettings - Navigate to settings screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllRemindersScreen(
    viewModel: ReminderViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAddReminder: () -> Unit,
    onNavigateToViewReminder: (Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    // Collect state from ViewModel
    val reminders by viewModel.filteredReminders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    // Local state for search field
    var searchText by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Load today's reminders when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadTodayReminders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        // Search TextField
                        TextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                                viewModel.searchReminders(it)
                            },
                            placeholder = { Text("Search reminders...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Column {
                            Text(
                                text = "Today",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatDate(selectedDate),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchText = ""
                            viewModel.searchReminders("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close search"
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        // Search icon
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }

                        // Calendar icon
                        IconButton(onClick = onNavigateToCalendar) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar"
                            )
                        }

                        // Settings icon
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            // Floating action button to add new reminder
            FloatingActionButton(
                onClick = onNavigateToAddReminder,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading state
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Empty state
                reminders.isEmpty() -> {
                    EmptyStateContent(
                        isSearchActive = isSearchActive,
                        onAddReminder = onNavigateToAddReminder
                    )
                }

                // Show reminders
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = reminders,
                            key = { reminder -> reminder.id }
                        ) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onClick = { onNavigateToViewReminder(reminder.id) }
                            )
                        }

                        // Bottom spacing for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state when no reminders exist
 */
@Composable
private fun EmptyStateContent(
    isSearchActive: Boolean,
    onAddReminder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearchActive) Icons.Default.SearchOff else Icons.Default.EventNote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearchActive) "No reminders found" else "No reminders for today",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSearchActive)
                "Try a different search term"
            else
                "Tap the + button to create your first reminder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )

        if (!isSearchActive) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddReminder,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Reminder")
            }
        }
    }
}

/**
 * Format date for display
 */
private fun formatDate(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val today = Calendar.getInstance()

    return when {
        // Check if it's today
        calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
            "Today, ${SimpleDateFormat("d MMMM", Locale.getDefault()).format(Date(timestamp))}"
        }
        else -> {
            SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
package com.example.reminderapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.ui.components.TopAppBarComponent
import com.example.reminderapp.ui.theme.*
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * View/Edit Reminder Screen
 * Displays reminder details with edit and delete functionality
 * Matches the "Look at reminder" screen in Figma prototype
 *
 * @param reminderId - ID of the reminder to display
 * @param viewModel - ViewModel managing app state
 * @param onNavigateBack - Navigate back to previous screen
 * @param onReminderDeleted - Callback when reminder is deleted
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReminderScreen(
    reminderId: Int,
    viewModel: ReminderViewModel,
    onNavigateBack: () -> Unit,
    onReminderDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for the reminder
    var reminder by remember { mutableStateOf<Reminder?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showUpdateSnackbar by remember { mutableStateOf(false) }

    // Editable fields
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf(Calendar.getInstance()) }
    var editTime by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf("") }
    var editColorCode by remember { mutableStateOf(0) }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Load reminder data
    LaunchedEffect(reminderId) {
        scope.launch {
            val loadedReminder = viewModel.getReminderById(reminderId)
            loadedReminder?.let {
                reminder = it
                // Initialize edit fields
                editTitle = it.title
                editDescription = it.description
                editDate.timeInMillis = it.date
                editTime = it.time
                editLocation = it.location
                editColorCode = it.colorCode
            }
        }
    }

    // Show snackbar when update is successful
    LaunchedEffect(showUpdateSnackbar) {
        if (showUpdateSnackbar) {
            snackbarHostState.showSnackbar(
                message = "Reminder updated successfully",
                duration = SnackbarDuration.Short
            )
            showUpdateSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = if (isEditMode) "Edit Reminder" else "Reminder Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    if (!isEditMode) {
                        // Edit button
                        IconButton(onClick = { isEditMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit reminder"
                            )
                        }

                        // Delete button
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete reminder",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Reminder?") },
                text = { Text("Are you sure you want to delete this reminder? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            reminder?.let {
                                viewModel.deleteReminder(it)
                                onReminderDeleted()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (reminder == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (isEditMode) {
                    // EDIT MODE - Editable fields
                    EditModeContent(
                        title = editTitle,
                        onTitleChange = { editTitle = it },
                        description = editDescription,
                        onDescriptionChange = { editDescription = it },
                        date = editDate,
                        onDateChange = { editDate = it },
                        time = editTime,
                        onTimeChange = { hour, minute ->
                            editTime = String.format("%02d:%02d", hour, minute)
                            editDate.set(Calendar.HOUR_OF_DAY, hour)
                            editDate.set(Calendar.MINUTE, minute)
                        },
                        location = editLocation,
                        onLocationChange = { editLocation = it },
                        colorCode = editColorCode,
                        onColorCodeChange = { editColorCode = it },
                        onSave = {
                            // Update the reminder
                            reminder?.let { original ->
                                val updatedReminder = original.copy(
                                    title = editTitle,
                                    description = editDescription,
                                    date = editDate.timeInMillis,
                                    time = editTime,
                                    location = editLocation,
                                    colorCode = editColorCode
                                )
                                viewModel.updateReminder(updatedReminder)
                                reminder = updatedReminder
                                isEditMode = false
                                showUpdateSnackbar = true
                            }
                        },
                        onCancel = {
                            // Reset edit fields to original values
                            reminder?.let {
                                editTitle = it.title
                                editDescription = it.description
                                editDate.timeInMillis = it.date
                                editTime = it.time
                                editLocation = it.location
                                editColorCode = it.colorCode
                            }
                            isEditMode = false
                        }
                    )
                } else {
                    // VIEW MODE - Display only
                    ViewModeContent(reminder = reminder!!)
                }
            }
        }
    }
}

/**
 * View mode - Display reminder details
 */
@Composable
private fun ViewModeContent(reminder: Reminder) {
    // Color indicator card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getReminderColor(reminder.colorCode).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Title
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date and Time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(reminder.date),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reminder.time,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    // Location (if available)
    if (reminder.location.isNotBlank()) {
        DetailCard(
            icon = Icons.Default.LocationOn,
            title = "Location",
            content = reminder.location
        )
    }

    // Description
    if (reminder.description.isNotBlank()) {
        DetailCard(
            icon = Icons.Default.Notes,
            title = "Notes",
            content = reminder.description
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Created date info
    Text(
        text = "Created ${formatDate(reminder.createdAt)}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Detail card for displaying information
 */
@Composable
private fun DetailCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Edit mode - Editable fields
 */
@Composable
private fun EditModeContent(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    date: Calendar,
    onDateChange: (Calendar) -> Unit,
    time: String,
    onTimeChange: (Int, Int) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    colorCode: Int,
    onColorCodeChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current

    // Title field
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Title") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Title, contentDescription = null)
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Date picker
    OutlinedTextField(
        value = formatDate(date.timeInMillis),
        onValueChange = { },
        label = { Text("Date") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showDatePicker(context, date, onDateChange)
            },
        enabled = false,
        readOnly = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null)
        },
        colors = TextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Time picker
    OutlinedTextField(
        value = time,
        onValueChange = { },
        label = { Text("Time") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                showTimePicker(context, date, onTimeChange)
            },
        enabled = false,
        readOnly = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
        },
        colors = TextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Location field
    OutlinedTextField(
        value = location,
        onValueChange = onLocationChange,
        label = { Text("Location") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Description field
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Notes") },
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        maxLines = 5,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Notes, contentDescription = null)
        }
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Color selection
    Text(
        text = "Select Color",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    ColorSelector(
        selectedColorCode = colorCode,
        onColorSelected = onColorCodeChange
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Action buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cancel button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("Cancel")
        }

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            enabled = title.isNotBlank()
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save")
        }
    }
}

/**
 * Color selector component
 */
@Composable
private fun ColorSelector(
    selectedColorCode: Int,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        0 to ReminderBlue,
        1 to ReminderPink,
        2 to ReminderGreen,
        3 to ReminderYellow,
        4 to ReminderPurple
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { (code, color) ->
            Box(
                modifier = Modifier
                    .size(if (selectedColorCode == code) 56.dp else 48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(code) },
                contentAlignment = Alignment.Center
            ) {
                if (selectedColorCode == code) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helper function to get reminder color
 */
private fun getReminderColor(colorCode: Int): Color {
    return when (colorCode) {
        0 -> ReminderBlue
        1 -> ReminderPink
        2 -> ReminderGreen
        3 -> ReminderYellow
        4 -> ReminderPurple
        else -> ReminderBlue
    }
}

/**
 * Show date picker dialog
 */
private fun showDatePicker(
    context: Context,
    initialDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance()
            newDate.set(year, month, dayOfMonth)
            onDateSelected(newDate)
        },
        initialDate.get(Calendar.YEAR),
        initialDate.get(Calendar.MONTH),
        initialDate.get(Calendar.DAY_OF_MONTH)
    ).show()
}

/**
 * Show time picker dialog
 */
private fun showTimePicker(
    context: Context,
    initialDate: Calendar,
    onTimeSelected: (Int, Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(hourOfDay, minute)
        },
        initialDate.get(Calendar.HOUR_OF_DAY),
        initialDate.get(Calendar.MINUTE),
        true // 24-hour format
    ).show()
}

/**
 * Format date for display
 */
private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(timestamp))
}
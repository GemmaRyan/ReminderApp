package com.example.reminderapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.reminderapp.navigation.Screen
import com.example.reminderapp.ui.components.TopAppBarComponent
import com.example.reminderapp.ui.theme.*
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Add Reminder Screen
 * Allows user to create a new reminder with optional photo attachment
 * Matches the "Add Reminder" screen in Figma prototype
 *
 * @param viewModel - ViewModel managing app state
 * @param navController - Navigation controller for camera navigation
 * @param onNavigateBack - Navigate back to previous screen
 * @param onReminderAdded - Callback when reminder is successfully added
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    navController: NavHostController,
    onNavigateBack: () -> Unit,
    onReminderAdded: () -> Unit
) {
    val context = LocalContext.current

    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf("${selectedDate.get(Calendar.HOUR_OF_DAY)}:${String.format("%02d", selectedDate.get(Calendar.MINUTE))}") }
    var location by remember { mutableStateOf("") }
    var selectedColorCode by remember { mutableStateOf(0) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    // Get photo from navigation if coming back from camera
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.get<String>("photo_uri")?.let { uri ->
            photoUri = uri
            savedStateHandle.remove<String>("photo_uri")
        }
    }

    // Error state
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showError by remember { mutableStateOf(false) }

    // Watch for errors
    LaunchedEffect(errorMessage) {
        showError = errorMessage != null
    }

    Scaffold(
        topBar = {
            TopAppBarComponent(
                title = "Add Reminder",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Error message
            if (showError && errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("Enter reminder title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date picker
            OutlinedTextField(
                value = formatDateDisplay(selectedDate),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker(context, selectedDate) { newDate ->
                            selectedDate = newDate
                        }
                    },
                enabled = false,
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null
                    )
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
                value = selectedTime,
                onValueChange = { },
                label = { Text("Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showTimePicker(context, selectedDate) { hour, minute ->
                            selectedTime = String.format("%02d:%02d", hour, minute)
                            selectedDate.set(Calendar.HOUR_OF_DAY, hour)
                            selectedDate.set(Calendar.MINUTE, minute)
                        }
                    },
                enabled = false,
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null
                    )
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
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                placeholder = { Text("Enter location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes") },
                placeholder = { Text("Add notes or description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null
                    )
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
                selectedColorCode = selectedColorCode,
                onColorSelected = { selectedColorCode = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Photo attachment section
            Text(
                text = "Attach Photo (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (photoUri != null) {
                        // Show preview
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = "Attached photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    navController.navigate(Screen.Camera.route)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Camera, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Retake")
                            }

                            OutlinedButton(
                                onClick = { photoUri = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Remove")
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                navController.navigate(Screen.Camera.route)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Take Photo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.insertReminder(
                        title = title,
                        description = description,
                        date = selectedDate.timeInMillis,
                        time = selectedTime,
                        colorCode = selectedColorCode,
                        location = location,
                        imagePath = photoUri ?: ""
                    )

                    // If no error, navigate back
                    if (errorMessage == null) {
                        onReminderAdded()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = title.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
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
 * Show date picker dialog
 */
private fun showDatePicker(
    context: android.content.Context,
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
    context: android.content.Context,
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
private fun formatDateDisplay(calendar: Calendar): String {
    return SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(calendar.time)
}
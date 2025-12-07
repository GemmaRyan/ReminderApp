package com.example.reminderapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.reminderapp.data.model.Reminder
import com.example.reminderapp.navigation.Screen
import com.example.reminderapp.ui.theme.*
import com.example.reminderapp.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    viewModel: ReminderViewModel,
    navController: NavHostController,
    onReminderSaved: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedColorCode by remember { mutableStateOf(0) }

    // DATE + TIME
    val selectedDate = remember { Calendar.getInstance() }
    var selectedTime by remember { mutableStateOf("00:00") }

    // CAMERA PHOTO
    val photoUri = navController.currentBackStackEntry?.savedStateHandle?.get<String>("photo_uri")

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
                .padding(16.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),   // ⭐ ENABLE SCROLLING
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ----------------------- TITLE -----------------------
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
            )

            // ----------------------- DESCRIPTION -----------------------
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
            )

            // ----------------------- LOCATION -----------------------
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
            )

            // ----------------------- DATE PICKER -----------------------
            OutlinedTextField(
                value = formatDate(selectedDate.timeInMillis),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDatePicker(context, selectedDate) { newDate ->
                            selectedDate.timeInMillis = newDate.timeInMillis
                        }
                    },
                enabled = false,
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )

            // ----------------------- TIME PICKER -----------------------
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
                leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) }
            )

            // ----------------------- COLOR SELECTOR -----------------------
            Text(
                text = "Select Color",
                style = MaterialTheme.typography.titleMedium
            )

            ColorSelector(
                selectedColorCode = selectedColorCode,
                onColorSelected = { selectedColorCode = it }
            )

            // ----------------------- PHOTO PREVIEW -----------------------
            if (photoUri != null) {
                Text("Photo attached ✔")
            }

            // ----------------------- TAKE PHOTO BUTTON -----------------------
            Button(
                onClick = { navController.navigate(Screen.Camera.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Take Photo")
            }

            // ----------------------- SAVE BUTTON -----------------------
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

                    // Reset camera state
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("photo_uri")

                    onReminderSaved()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Reminder")
            }

            Spacer(modifier = Modifier.height(20.dp)) // extra space at bottom
        }
    }
}

// --------------------------------------------------

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
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// --------------------------------------------------

private fun showDatePicker(
    context: android.content.Context,
    initialDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, day)
            onDateSelected(newCal)
        },
        initialDate.get(Calendar.YEAR),
        initialDate.get(Calendar.MONTH),
        initialDate.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePicker(
    context: android.content.Context,
    calendar: Calendar,
    onTimeSelected: (Int, Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(hour, minute) },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("EEEE, d MMM yyyy", Locale.getDefault())
        .format(Date(timestamp))
}
